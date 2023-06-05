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
package org.flowable.mongodb.persistence.mapper;

import org.bson.Document;
import org.bson.types.Binary;
import org.flowable.common.engine.impl.persistence.entity.ByteArrayEntityImpl;
import org.flowable.engine.impl.persistence.entity.ResourceEntityImpl;

/**
 * @author Alvaro Vallve
 */
public class ByteArrayEntityMapper extends AbstractEntityToDocumentMapper<ByteArrayEntityImpl> {

    @Override
    public ByteArrayEntityImpl fromDocument(Document document) {
    	ByteArrayEntityImpl byteArrayEntity = new ByteArrayEntityImpl();
        byteArrayEntity.setId(document.getString("_id"));
        byteArrayEntity.setName(document.getString("name"));

        Binary binary = (Binary) document.get("bytes");
        byteArrayEntity.setBytes(binary.getData());

        byteArrayEntity.setDeploymentId(document.getString("deploymentId"));
//        resourceEntity.setGenerated(document.getBoolean("generated"));
        return byteArrayEntity;
    }

    @Override
    public Document toDocument(ByteArrayEntityImpl byteArrayEntity) {
        // Note: no revision
        Document resourceDocument = new Document();
        appendIfNotNull(resourceDocument, "_id", byteArrayEntity.getId());
        appendIfNotNull(resourceDocument, "name", byteArrayEntity.getName());
        appendIfNotNull(resourceDocument, "bytes", byteArrayEntity.getBytes());
        appendIfNotNull(resourceDocument, "deploymentId", byteArrayEntity.getDeploymentId());
//        appendIfNotNull(resourceDocument, "generated", resourceEntity.isGenerated());
        return resourceDocument;
    }

}
