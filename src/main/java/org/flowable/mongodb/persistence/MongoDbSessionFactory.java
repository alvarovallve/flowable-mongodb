/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.mongodb.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.flowable.common.engine.impl.context.Context;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.interceptor.Session;
import org.flowable.common.engine.impl.interceptor.SessionFactory;
import org.flowable.common.engine.impl.persistence.cache.EntityCache;
import org.flowable.common.engine.impl.persistence.entity.ByteArrayEntityImpl;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.engine.impl.persistence.entity.ActivityInstanceEntityImpl;
import org.flowable.engine.impl.persistence.entity.CommentEntityImpl;
import org.flowable.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.impl.persistence.entity.HistoricActivityInstanceEntityImpl;
import org.flowable.engine.impl.persistence.entity.HistoricDetailEntityImpl;
import org.flowable.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl;
import org.flowable.engine.impl.persistence.entity.ResourceEntityImpl;
import org.flowable.eventsubscription.service.impl.persistence.entity.CompensateEventSubscriptionEntityImpl;
import org.flowable.eventsubscription.service.impl.persistence.entity.MessageEventSubscriptionEntityImpl;
import org.flowable.eventsubscription.service.impl.persistence.entity.SignalEventSubscriptionEntityImpl;
import org.flowable.identitylink.service.impl.persistence.entity.HistoricIdentityLinkEntityImpl;
import org.flowable.identitylink.service.impl.persistence.entity.IdentityLinkEntityImpl;
import org.flowable.job.service.impl.persistence.entity.JobEntityImpl;
import org.flowable.job.service.impl.persistence.entity.TimerJobEntityImpl;
import org.flowable.mongodb.persistence.entity.MongoDbModelEntityImpl;
import org.flowable.mongodb.persistence.entity.MongoDbProcessDefinitionEntityImpl;
import org.flowable.mongodb.persistence.manager.AbstractMongoDbDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbActivityInstanceDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbCommentDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbDeploymentDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbEventSubscriptionDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbExecutionDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbHistoricActivityInstanceDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbHistoricDetailDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbHistoricIdentityLinkDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbHistoricProcessInstanceDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbHistoricTaskInstanceDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbHistoricVariableInstanceDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbIdentityLinkDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbJobByteArrayDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbJobDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbModelDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbProcessDefinitionDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbResourceDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbTaskDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbTimerJobDataManager;
import org.flowable.mongodb.persistence.manager.MongoDbVariableInstanceDataManager;
import org.flowable.mongodb.persistence.mapper.ActivityInstanceEntityMapper;
import org.flowable.mongodb.persistence.mapper.ByteArrayEntityMapper;
import org.flowable.mongodb.persistence.mapper.CommentEntityMapper;
import org.flowable.mongodb.persistence.mapper.DeploymentEntityMapper;
import org.flowable.mongodb.persistence.mapper.EventSubscriptionEntityMapper;
import org.flowable.mongodb.persistence.mapper.ExecutionEntityMapper;
import org.flowable.mongodb.persistence.mapper.HistoricActivityInstanceEntityMapper;
import org.flowable.mongodb.persistence.mapper.HistoricDetailEntityMapper;
import org.flowable.mongodb.persistence.mapper.HistoricIdentityLinkEntityMapper;
import org.flowable.mongodb.persistence.mapper.HistoricProcessInstanceEntityMapper;
import org.flowable.mongodb.persistence.mapper.HistoricTaskInstanceEntityMapper;
import org.flowable.mongodb.persistence.mapper.HistoricVariableInstanceEntityMapper;
import org.flowable.mongodb.persistence.mapper.IdentityLinkEntityMapper;
import org.flowable.mongodb.persistence.mapper.JobByteArrayEntityMapper;
import org.flowable.mongodb.persistence.mapper.JobEntityMapper;
import org.flowable.mongodb.persistence.mapper.ModelEntityMapper;
import org.flowable.mongodb.persistence.mapper.ProcessDefinitionEntityMapper;
import org.flowable.mongodb.persistence.mapper.ResourceEntityMapper;
import org.flowable.mongodb.persistence.mapper.TaskEntityMapper;
import org.flowable.mongodb.persistence.mapper.TimerJobEntityMapper;
import org.flowable.mongodb.persistence.mapper.VariableInstanceEntityMapper;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntityImpl;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.flowable.variable.service.impl.persistence.entity.HistoricVariableInstanceEntityImpl;
import org.flowable.variable.service.impl.persistence.entity.VariableInstanceEntityImpl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * @author Joram Barrez
 */
public class MongoDbSessionFactory implements SessionFactory {

    protected MongoClient mongoClient;
    protected MongoDatabase mongoDatabase;
    protected ClientSessionProvider clientSessionProvider;

    protected Map<Class<? extends Entity>, EntityToDocumentMapper<? extends Entity>> entityMappers = new HashMap<>();
    protected Map<Class<? extends Entity>, String> classToCollectionMap = new HashMap<>();
    protected Map<String, EntityToDocumentMapper<? extends Entity>> collectionToMapperMap = new HashMap<>();
    protected Map<String, Class<? extends Entity>> collectionToClassMap = new HashMap<>();
    protected Map<String, AbstractMongoDbDataManager> collectionToDataManager = new HashMap<>();

    public MongoDbSessionFactory(MongoClient mongoClient, MongoDatabase mongoDatabase) {
        this(mongoClient, mongoDatabase, null);
    }

    public MongoDbSessionFactory(MongoClient mongoClient, MongoDatabase mongoDatabase, ClientSessionProvider clientSessionProvider) {
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoDatabase;
        this.clientSessionProvider = clientSessionProvider;

        initDefaultMappers();
    }

    protected void initDefaultMappers() {
        registerEntityMapper(ByteArrayEntityImpl.class,new ByteArrayEntityMapper(), MongoDbResourceDataManager.COLLECTION_BYTE_ARRAY);
        registerEntityMapper(DeploymentEntityImpl.class, new DeploymentEntityMapper(), MongoDbDeploymentDataManager.COLLECTION_DEPLOYMENT);
        registerEntityMapper(ResourceEntityImpl.class, new ResourceEntityMapper(), MongoDbResourceDataManager.COLLECTION_BYTE_ARRAY);
        registerEntityMapper(MongoDbProcessDefinitionEntityImpl.class, new ProcessDefinitionEntityMapper(), MongoDbProcessDefinitionDataManager.COLLECTION_PROCESS_DEFINITIONS);
        registerEntityMapper(ExecutionEntityImpl.class, new ExecutionEntityMapper(), MongoDbExecutionDataManager.COLLECTION_EXECUTIONS);
        registerEntityMapper(SignalEventSubscriptionEntityImpl.class, new EventSubscriptionEntityMapper(), MongoDbEventSubscriptionDataManager.COLLECTION_EVENT_SUBSCRIPTION);
        registerEntityMapper(MessageEventSubscriptionEntityImpl.class, new EventSubscriptionEntityMapper(), MongoDbEventSubscriptionDataManager.COLLECTION_EVENT_SUBSCRIPTION);
        registerEntityMapper(CompensateEventSubscriptionEntityImpl.class, new EventSubscriptionEntityMapper(), MongoDbEventSubscriptionDataManager.COLLECTION_EVENT_SUBSCRIPTION);
        registerEntityMapper(CommentEntityImpl.class, new CommentEntityMapper(), MongoDbCommentDataManager.COLLECTION_COMMENTS);
        registerEntityMapper(IdentityLinkEntityImpl.class, new IdentityLinkEntityMapper(), MongoDbIdentityLinkDataManager.COLLECTION_IDENTITY_LINKS);

        registerEntityMapper(TaskEntityImpl.class, new TaskEntityMapper(), MongoDbTaskDataManager.COLLECTION_TASKS);
        registerEntityMapper(HistoricTaskInstanceEntityImpl.class, new HistoricTaskInstanceEntityMapper(), MongoDbHistoricTaskInstanceDataManager.COLLECTION_HISTORIC_TASK_INSTANCES);
        registerEntityMapper(VariableInstanceEntityImpl.class, new VariableInstanceEntityMapper(), MongoDbVariableInstanceDataManager.COLLECTION_VARIABLES);
        registerEntityMapper(MongoDbModelEntityImpl.class, new ModelEntityMapper(), MongoDbModelDataManager.COLLECTION_MODELS);

        registerEntityMapper(JobEntityImpl.class, new JobEntityMapper(), MongoDbJobDataManager.COLLECTION_JOBS);
        registerEntityMapper(TimerJobEntityImpl.class, new TimerJobEntityMapper(), MongoDbTimerJobDataManager.COLLECTION_TIMER_JOBS);

        registerEntityMapper(HistoricProcessInstanceEntityImpl.class, new HistoricProcessInstanceEntityMapper(), MongoDbHistoricProcessInstanceDataManager.COLLECTION_HISTORIC_PROCESS_INSTANCES);
        registerEntityMapper(HistoricActivityInstanceEntityImpl.class, new HistoricActivityInstanceEntityMapper(), MongoDbHistoricActivityInstanceDataManager.COLLECTION_HISTORIC_ACTIVITY_INSTANCES);
        registerEntityMapper(ActivityInstanceEntityImpl.class, new ActivityInstanceEntityMapper(), MongoDbActivityInstanceDataManager.COLLECTION_ACTIVITY_INSTANCES);
        registerEntityMapper(HistoricIdentityLinkEntityImpl.class, new HistoricIdentityLinkEntityMapper(), MongoDbHistoricIdentityLinkDataManager.COLLECTION_HISTORIC_IDENTITY_LINKS);
        registerEntityMapper(HistoricVariableInstanceEntityImpl.class, new HistoricVariableInstanceEntityMapper(), MongoDbHistoricVariableInstanceDataManager.COLLECTION_HISTORIC_VARIABLE_INSTANCES);
        registerEntityMapper(HistoricDetailEntityImpl.class, new HistoricDetailEntityMapper(), MongoDbHistoricDetailDataManager.COLLECTION_HISTORIC_DETAILS);
    }

    @Override
    public Class<?> getSessionType() {
        return MongoDbSession.class;
    }

    @Override
    public Session openSession(CommandContext commandContext) {
        if (clientSessionProvider == null) {
            return new MongoDbSession(this, mongoClient, mongoDatabase, Context.getCommandContext().getSession(EntityCache.class));
        } else {
            return new MongoDbSession(this, mongoClient, mongoDatabase, Context.getCommandContext().getSession(EntityCache.class), clientSessionProvider.clientSession());
        }
    }

    public void registerEntityMapper(Class<? extends Entity> clazz, EntityToDocumentMapper<? extends Entity> mapper, String collection) {
        entityMappers.put(clazz, mapper);
        classToCollectionMap.put(clazz, collection);
        collectionToClassMap.put(collection, clazz);
        collectionToMapperMap.put(collection, mapper);
    }

    public void registerDataManager(String collection, AbstractMongoDbDataManager dataManager) {
        collectionToDataManager.put(collection, dataManager);
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public void setMongoDatabase(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    public Collection<String> getCollectionNames() {
        return classToCollectionMap.values();
    }

    public String getCollectionForEntityClass(Class<? extends Entity> clazz) {
        return classToCollectionMap.get(clazz);
    }

    public Map<Class<? extends Entity>, String> getClassToCollectionsMap() {
        return classToCollectionMap;
    }

    public void setClassToCollectionsMap(Map<Class<? extends Entity>, String> collections) {
        this.classToCollectionMap = collections;
    }

    public EntityToDocumentMapper<? extends Entity> getMapperForCollection(String collection) {
        return collectionToMapperMap.get(collection);
    }

    public Map<String, EntityToDocumentMapper<? extends Entity>> getCollectionToMapper() {
        return collectionToMapperMap;
    }

    public void setCollectionToMapper(Map<String, EntityToDocumentMapper<? extends Entity>> collectionToMapper) {
        this.collectionToMapperMap = collectionToMapper;
    }

    public Class<? extends Entity> getClassForCollection(String collection) {
        return collectionToClassMap.get(collection);
    }

    public AbstractMongoDbDataManager getDataManagerForCollection(String collection) {
        return collectionToDataManager.get(collection);
    }

    public Map<String, Class<? extends Entity>> getCollectionToClass() {
        return collectionToClassMap;
    }

    public void setCollectionToClass(Map<String, Class<? extends Entity>> collectionToClass) {
        this.collectionToClassMap = collectionToClass;
    }

    public EntityToDocumentMapper<? extends Entity> getMapperForEntityClass(Class<? extends Entity> clazz) {
        return entityMappers.get(clazz);
    }

    public Map<Class<? extends Entity>, EntityToDocumentMapper<? extends Entity>> getEntityMappers() {
        return entityMappers;
    }

    public void setEntityMappers(Map<Class<? extends Entity>, EntityToDocumentMapper<? extends Entity>> entityMappers) {
        this.entityMappers = entityMappers;
    }

}
