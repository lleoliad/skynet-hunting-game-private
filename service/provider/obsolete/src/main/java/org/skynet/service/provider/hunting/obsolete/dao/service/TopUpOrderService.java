package org.skynet.service.provider.hunting.obsolete.dao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.skynet.service.provider.hunting.obsolete.dao.entity.TopUpOrder;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author ${author}
 * @since 2022-10-19
 */
public interface TopUpOrderService extends IService<TopUpOrder> {

    IPage<TopUpOrder> showNeedSupplement(Page<TopUpOrder> pageParam);
}
