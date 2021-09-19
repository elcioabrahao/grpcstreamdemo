package br.com.via.grpcstreamdemo.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class MovieControllerServer {
    public static final int MOVIE_CONTROLLER_SERVICE_PORT = 5551;

    public static void main(String[] args)
            throws IOException, InterruptedException {
        Server server =
                ServerBuilder.forPort(MOVIE_CONTROLLER_SERVICE_PORT)
                        .addService(new MovieControllerServiceImpl())
                        .build();
        server.start();
        System.out.println("Servidor MovieController ONLINE!");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));
        server.awaitTermination();
    }
}