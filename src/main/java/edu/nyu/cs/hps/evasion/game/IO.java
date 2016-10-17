package edu.nyu.cs.hps.evasion.game;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class IO {

  private static class SocketInfo {
    public int index;
    public ServerSocket serverSocket;
    public Socket clientSocket;
    public ExecutorService pool;
    public PrintWriter in;
    public BufferedReader out;
    public String latestLine;
    public Future<Boolean> finish;
    public String name;
  }

  private List<SocketInfo> infoList;

  public void start(List<Integer> ports) throws Exception {
    infoList = new ArrayList<>();
    for(Integer port : ports) {
      SocketInfo info = new SocketInfo();
      info.pool = Executors.newFixedThreadPool(1);
      info.name = "port_" + port;
      info.serverSocket = new ServerSocket(port);
      info.index = infoList.size();
      infoList.add(info);
    }
    List<Future<Socket>> futures = new ArrayList<>();
    for(SocketInfo info : infoList) {
      futures.add(info.pool.submit(() -> info.serverSocket.accept()));
    }
    for(SocketInfo info : infoList) {
      info.clientSocket = futures.get(info.index).get();
      info.clientSocket.setTcpNoDelay(true);

      info.in = new PrintWriter(info.clientSocket.getOutputStream(), true);
      info.out = new BufferedReader(new InputStreamReader(info.clientSocket.getInputStream()));

      info.finish = info.pool.submit(() -> {
        try {
          String line;
          while ((line = info.out.readLine()) != null) {
            if(line.startsWith("name:")){
              info.name = line.substring(5).trim();
            } else {
              info.latestLine = line;
            }
          }
          return true;
        }
        catch (Exception e) {
          return false;
        }
      });
    }
  }

  public void sendLine(int index, String string){
    infoList.get(index).in.print(string + "\n");
    infoList.get(index).in.flush();
  }

  public void destroy() {
    try {
      for(SocketInfo info : infoList) {
        info.clientSocket.close();
        info.serverSocket.close();
        info.finish.get();
        info.pool.shutdown();
      }
    } catch (Exception e){
      System.out.println(e.getMessage());
    }
  }

  public String getLatestLine(int index) {
    return infoList.get(index).latestLine;
  }

  public String getName(int index){
    return infoList.get(index).name;
  }

}
