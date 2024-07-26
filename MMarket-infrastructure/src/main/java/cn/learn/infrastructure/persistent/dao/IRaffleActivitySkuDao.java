package cn.learn.infrastructure.persistent.dao;

import cn.learn.infrastructure.persistent.po.RaffleActivitySkuPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 商品sku dao
 * @create 2024-03-16 11:04
 */
@Mapper
public interface IRaffleActivitySkuDao {

    RaffleActivitySkuPO queryActivitySku(Long sku);

    /**
     * 更新操作，每次自减1
     * @param sku
     */
    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);

}
