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
package org.flowable.mongodb.persistence.manager;

import java.util.List;

import org.flowable.common.engine.impl.persistence.entity.ByteArrayEntity;
import org.flowable.common.engine.impl.persistence.entity.ByteArrayEntityImpl;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.common.engine.impl.persistence.entity.data.ByteArrayDataManager;
import org.flowable.engine.impl.persistence.entity.ResourceEntity;
import org.flowable.engine.impl.persistence.entity.ResourceEntityImpl;
import org.flowable.engine.impl.persistence.entity.data.ResourceDataManager;
import org.flowable.mongodb.cfg.MongoDbProcessEngineConfiguration;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;

/**
 * @author Alvaro Vallve
 */
public class MongoDbByteArrayDataManager extends AbstractMongoDbDataManager<ByteArrayEntity> implements ByteArrayDataManager {

    public static final String COLLECTION_BYTE_ARRAY = "byteArrays";

    public MongoDbByteArrayDataManager(MongoDbProcessEngineConfiguration processEngineConfiguration) {
        super(processEngineConfiguration);
    }

    @Override
    public String getCollection() {
        return COLLECTION_BYTE_ARRAY;
    }

    @Override
    public ByteArrayEntity create() {
        return new ByteArrayEntityImpl();
    }

    @Override
    public BasicDBObject createUpdateObject(Entity entity) {
    	throw new UnsupportedOperationException();
    }

	@Override
	public List<ByteArrayEntity> findAll() {
		return getMongoDbSession().find(COLLECTION_BYTE_ARRAY, null);
	}

	@Override
	public void deleteByteArrayNoRevisionCheck(String byteArrayEntityId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
