package org.skynet.service.provider.hunting.obsolete.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.skynet.service.provider.hunting.obsolete.controller.admin.RecordIndex;
import org.skynet.service.provider.hunting.obsolete.dao.mapper.RecordIndexMapper;
import org.skynet.service.provider.hunting.obsolete.service.RecordIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
@Transactional
public class RecordIndexServiceImpl extends ServiceImpl<RecordIndexMapper, RecordIndex> implements RecordIndexService {


    @Resource
    private RecordIndexMapper recordIndexMapper;

    @Override
    public void selectAndSave(List<RecordIndex> list) {
        QueryWrapper<RecordIndex> wrapper = new QueryWrapper<>();


    }


}
