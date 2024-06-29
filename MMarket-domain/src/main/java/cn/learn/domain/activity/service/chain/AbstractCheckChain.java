package cn.learn.domain.activity.service.chain;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 下单规则责任链抽象类
 * @create 2024-03-23 10:16
 */
public abstract class AbstractCheckChain implements ICheckChain {

    private ICheckChain next;

    @Override
    public ICheckChain next() {
        return next;
    }

    @Override
    public ICheckChain setNext(ICheckChain next) {
        this.next = next;
        return next;
    }

}
