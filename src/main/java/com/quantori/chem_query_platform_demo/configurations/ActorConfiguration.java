package com.quantori.chem_query_platform_demo.configurations;


import akka.actor.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AskPattern;
import com.quantori.cqp.api.model.Flattened;
import com.quantori.cqp.core.configuration.AkkaClusterProvider;
import com.quantori.cqp.core.configuration.ClusterConfigurationProperties;
import com.quantori.cqp.core.configuration.LocalClusterProvider;
import com.quantori.cqp.core.source.CqpService;
import com.quantori.cqp.core.source.SourceRootActor;
import com.quantori.cqp.core.task.TaskServiceActor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

/**
 * Configuration class for the application using Akka, Spring Boot, and Slick.
 * Sets up the actor system, clustering, and database session for task management.
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@RequiredArgsConstructor
@Slf4j
public class ActorConfiguration {

    private final ElasticSearchConfig elasticSearchConfig;

    /**
     * Creates and configures the Akka Actor system with clustering capabilities.
     *
     * @param maxAmountOfSearchActors Maximum number of search actors allowed.
     * @param hostName                Hostname for the Akka cluster.
     * @param port                    Port for the Akka cluster.
     * @param nodes                   Comma-separated list of cluster seed nodes.
     * @param applicationName         Application name used for the Akka system.
     * @return Configured Akka actor system.
     */
    @Bean
    public akka.actor.typed.ActorSystem<SourceRootActor.Command> actorTypedSystem(
            @Value("${app.search.actors.max:100}") int maxAmountOfSearchActors,
            @Value("${app.cluster.hostname:'localhost'}") String hostName,
            @Value("${app.cluster.port:2551}") int port,
            @Value("${app.cluster.nodes:'localhost:2551'}") String nodes,
            @Value("${spring.application.name}") String applicationName) {
        AkkaClusterProvider clusterProvider = new LocalClusterProvider();

        ClusterConfigurationProperties clusterConfigurationProperties
                = ClusterConfigurationProperties.builder()
                .systemName(applicationName)
                .maxSearchActors(maxAmountOfSearchActors)
                .clusterHostName(hostName)
                .clusterPort(port)
                .seedNodes(Arrays.asList(nodes.split(","))).build();
        return clusterProvider.actorTypedSystem(clusterConfigurationProperties);
    }

    /**
     * Creates a task actor using the Akka actor system.
     *
     * @param system The Akka actor system.
     * @return ActorRef for the TaskServiceActor.
     */
    @Bean
    public ActorRef<TaskServiceActor.Command> taskActor(akka.actor.typed.ActorSystem<SourceRootActor.Command> system) {
        return AskPattern.ask(system, (ActorRef<SourceRootActor.StartedActor<TaskServiceActor.Command>> replyTo) ->
                        new SourceRootActor.StartActor<>(TaskServiceActor.create(), replyTo),
                Duration.ofMinutes(1),
                system.scheduler()).toCompletableFuture().join().actorRef;
    }

    /**
     * Retrieves the classic Akka ActorSystem from the typed actor system.
     *
     * @param system The typed Akka actor system.
     * @return Classic ActorSystem instance.
     */
    @Bean
    public ActorSystem actorSystem(akka.actor.typed.ActorSystem<SourceRootActor.Command> system) {
        return system.classicSystem();
    }

    @Bean
    public CqpService<?, ?, Flattened.Molecule, Flattened.Molecule> moleculeService(
            akka.actor.typed.ActorSystem<SourceRootActor.Command> system
    ) {
        return new CqpService<>(Map.of(elasticSearchConfig.storageType(), elasticSearchConfig.getStorageMolecules()),
                Integer.MAX_VALUE, system);
    }


    @Bean
    public ActorSystemShutdownHelper actorSystemShutdownHelper(ActorSystem actorSystem) {
        return new ActorSystemShutdownHelper(actorSystem);
    }

    public static class ActorSystemShutdownHelper implements SmartLifecycle {
        private final ActorSystem actorSystem;

        public ActorSystemShutdownHelper(ActorSystem actorSystem) {
            this.actorSystem = actorSystem;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
            throw new RuntimeException("The stop method must never be called");
        }

        @Override
        public void stop(@NonNull Runnable callback) {
            if (actorSystem.whenTerminated().isCompleted()) {
                callback.run();
            } else {
                actorSystem.registerOnTermination(callback);
                actorSystem.terminate();
            }
        }

        @Override
        public boolean isRunning() {
            return !actorSystem.whenTerminated().isCompleted();
        }

        @Override
        public int getPhase() {
            return -10;
        }
    }
}
