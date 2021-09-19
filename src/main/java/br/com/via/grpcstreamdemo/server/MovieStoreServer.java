package br.com.via.grpcstreamdemo.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class MovieStoreServer {
    public static final int MOVIE_STORE_SERVICE_PORT = 5552;

    public static void main(String[] args)
            throws IOException, InterruptedException {
        Server server =
                ServerBuilder.forPort(MOVIE_STORE_SERVICE_PORT)
                        .addService(new MovieStoreServiceImpl())
                        .build();
        server.start();
        System.out.println("Servidor MovieStore ONLINE!");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));
        server.awaitTermination();
    }
}