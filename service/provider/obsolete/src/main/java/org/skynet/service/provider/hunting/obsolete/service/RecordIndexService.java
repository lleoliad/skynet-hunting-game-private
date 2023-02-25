package org.skynet.service.provider.hunting.obsolete.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.skynet.service.provider.hunting.obsolete.controller.admin.RecordIndex;

import java.util.List;

public interface RecordIndexService extends IService<RecordIndex> {

    void selectAndSave(List<RecordIndex> list);

}
