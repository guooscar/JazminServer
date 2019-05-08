package jazmin.driver.mongodb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.DumpUtil;

/**
 * 
 * @author skydu
 *
 */
public abstract class MongoBeanDAO<T> extends MongoDAO{
	//
	private static Logger logger=LoggerFactory.get(MongoBeanDAO.class);
	//
	protected Class<T> typeClass;
	//
	protected String collectionName;

	//
	public MongoBeanDAO() {
		typeClass=getTypeClass();
		collectionName = typeClass.getSimpleName();
	}
	//
	public List<Document> listIndexes(){
		List<Document> documents=new ArrayList<>();
		ListIndexesIterable<Document> list=getCollection().listIndexes();
		for (Document e : list) {
			if(logger.isInfoEnabled()) {
				logger.info("listIndexes clazz:{} index:{}",
						typeClass.getName(),DumpUtil.dump(e));
			}
			documents.add(e);
		}
		return documents;
	}
	//
	public void createUniqueIndex(String field) {
		createIndex(true,new String[]{field});
	}
	//
	protected void createIndex(String field) {
		createIndex(new String[]{field});
	}
	//
	protected void createUniqueIndex(String ... fields) {
		createIndex(true, fields);
	}
	//
	protected void createIndex(String ... fields) {
		createIndex(false, fields);
	}
	//
	protected void createIndex(boolean unique,String ... fields) {
		if(fields==null||fields.length==0) {
			return;
		}
		StringBuilder name=new StringBuilder();
		for (String field : fields) {
			name.append(field).append("_1_");
		}
		name.deleteCharAt(name.length()-1);
		List<Document> indexs=listIndexes();
		for (Document e : indexs) {
			if(e.get("name").equals(name.toString())) {//已存在
				return;
			}
		}
		createIndex(
			Indexes.ascending(fields),
			new IndexOptions().unique(unique)
		);
	}
	//
	protected void createIndex(Bson keys,IndexOptions options) {
		long startTime=System.currentTimeMillis();
		String result=getCollection().createIndex(keys, options);
		if(logger.isDebugEnabled()) {
			logger.debug("createIndex using {}ms \nkeys:{} \noptions:{} \nresult:{}",
					System.currentTimeMillis()-startTime,
					toBsonDocument(keys),
					DumpUtil.dump(options),
					result
					);
		}
	}
	//
	public abstract void init();
	//
	protected static final HashSet<Class<?>> WRAP_TYPES = new HashSet<>();
	static {
		WRAP_TYPES.add(Boolean.class);
		WRAP_TYPES.add(Character.class);
		WRAP_TYPES.add(Byte.class);
		WRAP_TYPES.add(Short.class);
		WRAP_TYPES.add(Integer.class);
		WRAP_TYPES.add(Long.class);
		WRAP_TYPES.add(BigDecimal.class);
		WRAP_TYPES.add(BigInteger.class);
		WRAP_TYPES.add(Double.class);
		WRAP_TYPES.add(Float.class);
		WRAP_TYPES.add(String.class);
		WRAP_TYPES.add(Date.class);
		WRAP_TYPES.add(Timestamp.class);
		WRAP_TYPES.add(java.sql.Date.class);
		WRAP_TYPES.add(Byte[].class);
		WRAP_TYPES.add(byte[].class);
		WRAP_TYPES.add(int.class);
		WRAP_TYPES.add(boolean.class);
		WRAP_TYPES.add(char.class);
		WRAP_TYPES.add(byte.class);
		WRAP_TYPES.add(short.class);
		WRAP_TYPES.add(int.class);
		WRAP_TYPES.add(long.class);
		WRAP_TYPES.add(float.class);
		WRAP_TYPES.add(double.class);
		WRAP_TYPES.add(ObjectId.class);
	}
	//
	protected MongoCollection<T> getCollection() {
		return getCollection(typeClass,collectionName);
	}
	//
	protected MongoCollection<Document> getDocCollection() {
		return getCollection(collectionName);
	}
	//
	protected Document createDocument(T bean,String ... excludeFields) {
		Set<String> excludeFieldSet=new HashSet<>();
		if(excludeFields!=null) {
			for (String excludeField : excludeFields) {
				excludeFieldSet.add(excludeField);
			}
		}
		Document doc = new Document();
		Field[] fields = bean.getClass().getFields();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			if(excludeFieldSet.contains(field.getName())) {
				continue;
			}
			try {
				doc.append(field.getName(), field.get(bean));
			} catch (Exception e) {
				throw new MongoException(e);
			}
		}
		return doc;
	}

	//
	private List<Field> getNoStaticFinalFields(Class<?> clazz) {
		List<Field> fieldList = new ArrayList<>();
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
				continue;
			}
			fieldList.add(field);
		}
		return fieldList;
	}

	//
	protected Object converBean(Object o, Document doc) throws Exception {
		Class<?> type = getTypeClass();
		List<Field> fields = getNoStaticFinalFields(type);
		for (Field f : fields) {
			String fieldName = f.getName();
			Class<?> fieldType = f.getType();
			if (!WRAP_TYPES.contains(fieldType)) {
				continue;
			}

			Object value = null;
			if (fieldType.equals(String.class)) {
				value = doc.getString(fieldName);
			} else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
				value = doc.getInteger(fieldName);
			} else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
				value = doc.getInteger(fieldName);
			} else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
				value = doc.getLong(fieldName);
			} else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
				value = doc.getDouble(fieldName);
			} else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
				value = doc.get(fieldName);
			} else if (fieldType.equals(Date.class)) {
				value = doc.getDate(fieldName);
			} else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
				value = doc.getBoolean(fieldName);
			} else if (fieldType.equals(ObjectId.class)) {
				value = doc.getObjectId(fieldName);
			}
			f.setAccessible(true);
			if (value != null) {
				f.set(o, value);
			}
		}
		return o;
	}

	//
	protected Class<T> getTypeClass() {
		ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>) pt.getActualTypeArguments()[0];
		return type;
	}

	/**
	 * 
	 * @param bean
	 * @return
	 */
	protected ObjectId getObjectId(T bean) {
		Field field=null;
		try {
			field = bean.getClass().getField("_id");
			return (ObjectId) field.get(bean);
		} catch (Throwable e) {
			throw new MongoException(e);
		}
	}
	//
	protected Object getFieldValue(Object obj,Field field) {
		try {
			return field.get(obj);
		} catch (Throwable e) {
			throw new MongoException(e);
		}
	}
	//
	protected void setFieldValue(Object obj,Field field,Object value) {
		try {
			field.set(obj, value);
		} catch (Throwable e) {
			throw new MongoException(e);
		}
	}
	//
	protected void setFieldValue(Object obj,String fieldName,Object value) {
		try {
			setFieldValue(obj, obj.getClass().getField(fieldName),value);
		} catch (Throwable e) {
			throw new MongoException(e);
		}
	}
	//
	protected Object getFieldValue(Object obj,String field) {
		try {
			return getFieldValue(obj,obj.getClass().getField(field));
		} catch (Throwable e) {
			throw new MongoException(e);
		}
	}
	/**
	 * 
	 * @param bean
	 */
	public void insert(T bean) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection=getCollection();
		collection.insertOne(bean);
		if(logger.isDebugEnabled()) {
			logger.debug("insert {} using {}ms\nbean:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					DumpUtil.dump(bean));
		}
	}
	
	/**
	 * 
	 * @param list
	 */
	public void insertMany(List<T> list) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection=getCollection();
		collection.insertMany(list);
		if(logger.isDebugEnabled()) {
			logger.debug("insertMany {} using {}ms\nsize:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					list.size());
		}
	}
	
	/**
	 * 
	 * @param bean
	 */
	public void insertReturn_Id(T bean) {
		long startTime=System.currentTimeMillis();
		MongoCollection<Document> collection=getDocCollection();
		Document doc = createDocument(bean,"_id");
		collection.insertOne(doc);
		setFieldValue(bean, "_id", doc.get( "_id" ));
		if(logger.isDebugEnabled()) {
			logger.debug("insert {} using {}ms\nbean:{} \ndoc:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					DumpUtil.dump(bean),
					DumpUtil.dump(doc));
		}
	}
	//
	/**
	 * 
	 * @param bean
	 * @param includeFields
	 */
	public long update(T bean,String ... includeFields) {
		long startTime=System.currentTimeMillis();
		if(includeFields==null||includeFields.length==0) {
			throw new MongoException("update field cannot be null");
		}
		Set<String> fieldSet=new HashSet<>();
		for (String field : includeFields) {
			fieldSet.add(field);
		}
	    Document setData = new Document();
	    List<Field> fields=getNoStaticFinalFields(bean.getClass());
	    for (Field field : fields) {
	    		if(!fieldSet.contains(field.getName())){
	    			continue;
	    		}
	    		setData.append(field.getName(), getFieldValue(bean, field));
		}
	    Document update = new Document();
	    update.append("$set", setData);
	    ObjectId id=(ObjectId) getFieldValue(bean, "_id");
	    MongoCollection<T> collection=getCollection();
	    UpdateResult result=collection.updateOne(Filters.eq("_id",id), update);
	    long modifiedCount=result.getModifiedCount();
        if(logger.isDebugEnabled()) {
			logger.debug("updateOne {} using {}ms modifiedCount:{}\nid:{}\nbean:{}\nupdate:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					modifiedCount,
					id.toHexString(),
					DumpUtil.dump(bean),
					DumpUtil.dump(update));
		}
        return modifiedCount;
	}
	
	/**
	 * 
	 * @param id
	 * @param update
	 */
	public long updateById(ObjectId id,Document update) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection=getCollection();
		UpdateResult result=collection.updateOne(Filters.eq("_id",id), update);
		long modifiedCount=result.getModifiedCount();
		if(logger.isDebugEnabled()) {
			logger.debug("updateOne {} using {}ms modifiedCount:{}\nid:{}\nupdate:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					modifiedCount,
					id.toHexString(),
					DumpUtil.dump(update));
		}
		return modifiedCount;
	}
	
	/**
	 * 
	 * @param id
	 * @param update
	 * @return
	 */
	public long update(Bson filters,Document update) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection=getCollection();
		UpdateResult result=collection.updateOne(filters, update);
		long modifiedCount=result.getModifiedCount();
		if(logger.isDebugEnabled()) {
			logger.debug("updateOne {} using {}ms modifiedCount:{}\nfilters:{}\nupdate:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					modifiedCount,
					toBsonDocument(filters),
					DumpUtil.dump(update));
		}
		return modifiedCount;
	}
	
	/**
	 * 
	 * @param filters
	 * @return
	 */
	public long deleteOne(Bson filters) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection=getCollection();
		DeleteResult result=collection.deleteOne(filters);
		long deletedCount=result.getDeletedCount();
		if(logger.isDebugEnabled()) {
			logger.debug("deleteOne {} using {}ms deletedCount:{}\nfilters:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					deletedCount,
					toBsonDocument(filters));
		}
		return deletedCount;
	}
	
	/**
	 * 删除
	 * @param id
	 */
	public long deleteById(ObjectId id) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection=getCollection();
		DeleteResult result=collection.deleteOne(Filters.eq("_id", id));
		long deletedCount=result.getDeletedCount();
		if(logger.isDebugEnabled()) {
			logger.debug("deleteOne {} using {}ms deletedCount:{}\nid:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					deletedCount,
					id.toHexString());
		}
		return deletedCount;
	}
	
	/**
	 * 
	 * @param filters
	 * @return
	 */
	public long deleteMany(Bson filters) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection=getCollection();
		DeleteResult result=collection.deleteMany(filters);
		long deletedCount=result.getDeletedCount();
		if(logger.isDebugEnabled()) {
			logger.debug("deleteMany {} using {}ms deletedCount:{}\nid:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					deletedCount,
					toBsonDocument(filters));
		}
		return deletedCount;
	}
	/**
	 * 
	 * @param filters
	 * @return
	 */
	public long deleteMany(Bson filters,DeleteOptions options) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection=getCollection();
		DeleteResult result=collection.deleteMany(filters,options);
		long deletedCount=result.getDeletedCount();
		if(logger.isDebugEnabled()) {
			logger.debug("deleteMany {} using {}ms deletedCount:{}\nid:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					deletedCount,
					toBsonDocument(filters));
		}
		return deletedCount;
	}

	/**
	 * 
	 * @param filter
	 * @return
	 */
	public T findOne(Bson filter) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection = getCollection();
		T ret = collection.find(filter).first();
		if(logger.isDebugEnabled()) {
			logger.debug("findOne {} using {}ms filter:{}\nret:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					toBsonDocument(filter),
					DumpUtil.dump(ret));
		}
		return ret;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public T findById(ObjectId id) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection = getCollection();
		T ret = collection.find(Filters.eq("_id", id)).first();
		if(logger.isDebugEnabled()) {
			logger.debug("findOne {} using {}ms id:{}\nret:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					id.toHexString(),
					DumpUtil.dump(ret));
		}
		return ret;
	}

	/**
	 * 
	 * @param filter
	 * @return
	 */
	public List<T> findList(Bson filter) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection = getCollection();
		FindIterable<T> iterable = collection.find(filter);
		try {
			List<T> list=new ArrayList<>();
			iterable.forEach(new Block<T>() {
				@Override
				public void apply(T t) {
					try {
						list.add(t);
					} catch (Throwable e) {
						throw new MongoException(e);
					}
				}
			});
			if(logger.isDebugEnabled()) {
				logger.debug("findList {} using {}ms \nfilter:{}\nret:{}",
						typeClass.getSimpleName(),
						System.currentTimeMillis()-startTime,
						toBsonDocument(filter),
						DumpUtil.dump(list));
			}
			return list;
		} catch (Exception e) {
			throw new MongoException(e);
		}
	}
	
	/**
	 * 
	 * @param filter
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<T> findList(Bson filter,Integer pageSize) {
		return findList(filter, null, null, 0, pageSize,null);
	}
	/**
	 * 
	 * @param clazz
	 * @param filter
	 * @param projection
	 * @param pageIndex 从0开始
	 * @param pageSize
	 * @return
	 */
	public List<T> findList(Bson filter,Bson projection,Bson orderBy,Integer pageIndex,Integer pageSize,Boolean noCursorTimeout) {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection = getCollection();
		FindIterable<T> iterable = collection.find();
		if(filter!=null) {
			iterable.filter(filter);
		}
		if(projection!=null) {
			iterable.projection(projection);
		}
		if(orderBy!=null) {
			iterable.sort(orderBy);
		}
		if(noCursorTimeout!=null) {
			iterable.noCursorTimeout(noCursorTimeout);
		}
		if(pageSize!=null) {
			if(pageIndex!=null) {
				iterable.skip(pageIndex*pageSize).limit(pageSize);
			}else {
				iterable.limit(pageSize);
			}
		}
		try {
			List<T> list=new ArrayList<>();
			iterable.forEach(new Block<T>() {
				@Override
				public void apply(T t) {
					try {
						list.add(t);
					} catch (Throwable e) {
						throw new MongoException(e);
					}
				}
			});
			if(logger.isDebugEnabled()) {
				logger.debug("findList {} using {}ms \nfilter:{} \nprojection:{} "
						+ "\norderBy:{} \npageIndex:{} \npageSize:{}\n noCursorTimeout:{} \nret:{}",
						typeClass.getSimpleName(),
						System.currentTimeMillis()-startTime,
						toBsonDocument(filter),
						DumpUtil.dump(projection),
						DumpUtil.dump(orderBy),
						pageIndex,
						pageSize,
						noCursorTimeout,
						DumpUtil.dump(list));
			}else if(logger.isInfoEnabled()) {
				logger.info("findList {} using {}ms",
						typeClass.getSimpleName(),
						System.currentTimeMillis()-startTime);
			}
			return list;
		} catch (Exception e) {
			throw new MongoException(e);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public List<T> findAll() {
		long startTime=System.currentTimeMillis();
		MongoCollection<T> collection = getCollection();
		FindIterable<T> iterable = collection.find();
		try {
			List<T> list=new ArrayList<>();
			iterable.forEach(new Block<T>() {
				@Override
				public void apply(T t) {
					try {
						list.add(t);
					} catch (Throwable e) {
						throw new MongoException(e);
					}
				}
			});
			if(logger.isDebugEnabled()) {
				logger.debug("findList {} using {}ms \nret:{}",
						typeClass.getSimpleName(),
						System.currentTimeMillis()-startTime,
						list.size());
			}
			return list;
		} catch (Exception e) {
			throw new MongoException(e);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public long count() {
		long startTime=System.currentTimeMillis();
		MongoCollection<Document> collection = getDocCollection();
		long count=collection.count();
		if(logger.isDebugEnabled()) {
			logger.debug("count {} using {}ms \nret:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					count);
		}
		return count;
	}
	
	/**
	 * 
	 * @param filter
	 * @return
	 */
	public long count(Bson filter) {
		long startTime=System.currentTimeMillis();
		MongoCollection<Document> collection = getDocCollection();
		long count=collection.count(filter);
		if(logger.isDebugEnabled()) {
			logger.debug("count {} using {}ms \nfilter:{} \nret:{}",
					typeClass.getSimpleName(),
					System.currentTimeMillis()-startTime,
					toBsonDocument(filter),
					count);
		}
		return count;
	}
	//
	public String toBsonDocument(Bson bson) {
		return DumpUtil.dump(bson.toBsonDocument(typeClass, pojoCodecRegistry));
	}
}
