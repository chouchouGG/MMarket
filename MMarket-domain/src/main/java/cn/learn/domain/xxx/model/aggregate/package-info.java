/**
 * 聚合对象主要用于保证事务。
 * note 聚合对象；定义了一个一致性边界，用于保证数据的一致性
 *  1. 聚合实体和值对象
 *  2. 聚合是聚合的对象，和提供基础处理对象的方法。但不建议在聚合中引入仓储和接口来做过大的逻辑。而这些复杂的操作应该放到service中处理
 *  3. 对象名称 XxxAggregate
 */
package cn.learn.domain.xxx.model.aggregate;