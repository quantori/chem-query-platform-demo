package com.quantori.chem_query_platform_demo.configurations;

import akka.actor.typed.ActorRef;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import akka.stream.alpakka.slick.javadsl.SlickSession$;
import com.quantori.cqp.core.source.SourceRootActor;
import com.quantori.cqp.core.task.TaskServiceActor;
import com.quantori.cqp.core.task.dao.TaskStatusDao;
import com.quantori.cqp.core.task.service.StreamTaskService;
import com.quantori.cqp.core.task.service.StreamTaskServiceImpl;
import com.quantori.cqp.core.task.service.TaskPersistenceService;
import com.quantori.cqp.core.task.service.TaskPersistenceServiceImpl;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories
public class AkkaTaskManagementConfiguration {

    /**
     * Creates a Slick database session with PostgreSQL.
     *
     * @param url      Database URL.
     * @param username Database username.
     * @param password Database password.
     * @return Slick session configured for PostgreSQL.
     * @see TaskStatusDao
     */
    @Bean
    public SlickSession slickSession(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        HashMap<String, String> map = new HashMap<>();
        map.put("profile", "slick.jdbc.PostgresProfile$");
        map.put("db.dataSourceClass", "slick.jdbc.DriverDataSource");
        map.put("db.properties.driver", "org.postgresql.Driver");
        map.put("db.properties.url", url);
        map.put("db.properties.user", username);
        map.put("db.properties.password", password);

        Config config = ConfigFactory.parseMap(map);
        return SlickSession$.MODULE$.forConfig(config);
    }

    /**
     * Provides a DAO for task status management.
     *
     * @param slickSession The Slick session.
     * @param actorSystem  The Akka actor system.
     * @return TaskStatusDao instance.
     * @see TaskPersistenceService
     */
    @Bean
    public TaskStatusDao taskStatusDao(SlickSession slickSession, akka.actor.typed.ActorSystem<?> actorSystem) {
        return new TaskStatusDao(slickSession, actorSystem);
    }

    /**
     * Creates a task persistence service for managing tasks.
     *
     * @param actorSystem The Akka actor system.
     * @param taskActor   ActorRef for task handling.
     * @param taskStatusDao Data access object for task statuses.
     * @param entityHolder The application context for entity management.
     * @return TaskPersistenceService instance.
     */
    @Bean
    public TaskPersistenceService taskPersistenceService(akka.actor.typed.ActorSystem<SourceRootActor.Command> actorSystem,
                                                         ActorRef<TaskServiceActor.Command> taskActor,
                                                         TaskStatusDao taskStatusDao, ApplicationContext entityHolder) {
        return new TaskPersistenceServiceImpl(actorSystem, taskActor,
                () -> streamTaskService(actorSystem, taskActor, taskStatusDao, entityHolder), taskStatusDao,
                entityHolder, false);
    }

    /**
     * Creates a stream task service for handling streaming tasks.
     *
     * @param actorSystem The Akka actor system.
     * @param taskActor   ActorRef for task handling.
     * @param taskStatusDao Data access object for task statuses.
     * @param entityHolder The application context for entity management.
     * @return StreamTaskService instance.
     */
    @Bean
    public StreamTaskService streamTaskService(akka.actor.typed.ActorSystem<SourceRootActor.Command> actorSystem,
                                               ActorRef<TaskServiceActor.Command> taskActor,
                                               TaskStatusDao taskStatusDao, ApplicationContext entityHolder) {
        return new StreamTaskServiceImpl(actorSystem, taskActor,
                () -> taskPersistenceService(actorSystem, taskActor, taskStatusDao, entityHolder));
    }
}