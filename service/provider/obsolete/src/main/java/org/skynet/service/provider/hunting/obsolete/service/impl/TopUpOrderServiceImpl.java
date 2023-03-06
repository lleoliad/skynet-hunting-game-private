package org.skynet.service.provider.hunting.obsolete.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.skynet.service.provider.hunting.obsolete.mapper.TopUpOrderMapper;
import org.skynet.service.provider.hunting.obsolete.pojo.entity.TopUpOrder;
import org.skynet.service.provider.hunting.obsolete.service.TopUpOrderService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
public class TopUpOrderServiceImpl extends ServiceImpl<TopUpOrderMapper, TopUpOrder> implements TopUpOrderService {

    @Override
    public IPage<TopUpOrder> showNeedSupplement(Page<TopUpOrder> pageParam) {


        QueryWrapper<TopUpOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("order_omit_state", 0);

        return baseMapper.selectPage(pageParam, queryWrapper);
    }
}
