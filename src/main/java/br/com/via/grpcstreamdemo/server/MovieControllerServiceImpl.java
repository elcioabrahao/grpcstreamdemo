package br.com.via.grpcstreamdemo.server;

import br.com.via.grpcstreamdemo.moviecontroller.MovieControllerServiceGrpc;
import br.com.via.grpcstreamdemo.moviecontroller.MovieRequest;
import br.com.via.grpcstreamdemo.moviecontroller.MovieResponse;
import br.com.via.grpcstreamdemo.moviestore.MovieStoreRequest;
import br.com.via.grpcstreamdemo.moviestore.MovieStoreServiceGrpc;
import br.com.via.grpcstreamdemo.recommender.RecommenderRequest;
import br.com.via.grpcstreamdemo.recommender.RecommenderResponse;
import br.com.via.grpcstreamdemo.recommender.RecommenderServiceGrpc;
import br.com.via.grpcstreamdemo.userpreferences.UserPreferencesRequest;
import br.com.via.grpcstreamdemo.userpreferences.UserPreferencesResponse;
import br.com.via.grpcstreamdemo.userpreferences.UserPreferencesServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MovieControllerServiceImpl extends
        MovieControllerServiceGrpc.MovieControllerServiceImplBase {
    public static final int MOVIES_SERVICE_PORT = 5552;
    public static final int USER_PREFERENCES_SERVICE_PORT = 5553;
    public static final int RECOMMENDER_SERVICE_PORT = 5554;

    @Override
    public void getMovie(MovieRequest request,
                         StreamObserver<MovieResponse>
                                 responseObserver) {
        String userId = request.getUserid();
        MovieStoreServiceGrpc.MovieStoreServiceBlockingStub
                movieStoreClient =
                MovieStoreServiceGrpc
                        .newBlockingStub(getChannel(MOVIES_SERVICE_PORT));
        UserPreferencesServiceGrpc.UserPreferencesServiceStub
                userPreferencesClient = UserPreferencesServiceGrpc
                .newStub(getChannel(USER_PREFERENCES_SERVICE_PORT));
        RecommenderServiceGrpc.RecommenderServiceStub
                recommenderClient =
                RecommenderServiceGrpc
                        .newStub(getChannel(RECOMMENDER_SERVICE_PORT));

        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<RecommenderRequest>
                recommenderRequestObserver =
                recommenderClient

                        .getRecommendedMovie(new StreamObserver<RecommenderResponse>() {
                            public void onNext(RecommenderResponse value) {
                                responseObserver.onNext(MovieResponse
                                        .newBuilder()
                                        .setMovie(value.getMovie()).build());
                                System.out.println("Recommended movie " +
                                        value.getMovie());
                            }

                            public void onError(Throwable t) {
                                responseObserver.onError(t);
                                latch.countDown();
                            }

                            public void onCompleted() {
                                responseObserver.onCompleted();
                                latch.countDown();
                            }
                        });
        StreamObserver<UserPreferencesRequest>
                streamObserver =
                userPreferencesClient
                        //.withWaitForReady()
                        .getShortlistedMovies(new StreamObserver<UserPreferencesResponse>() {
                            public void onNext(UserPreferencesResponse value) {
                                recommenderRequestObserver
                                        .onNext(RecommenderRequest.newBuilder()
                                                .setUserid(userId)
                                                .setMovie(value.getMovie()).build());
                            }

                            public void onError(Throwable t) {
                            }

                            @Override
                            public void onCompleted() {
                                recommenderRequestObserver.onCompleted();
                            }
                        });
        movieStoreClient
                //.withWaitForReady()
                .getMovies(MovieStoreRequest.newBuilder()
                        .setGenre(request.getGenre()).build())
                .forEachRemaining(response -> {
                    streamObserver
                            .onNext(UserPreferencesRequest.newBuilder()
                                    .setUserid(userId).setMovie(response.getMovie())
                                    .build());
                });
        streamObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ManagedChannel getChannel(int port) {
        return ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
    }
}