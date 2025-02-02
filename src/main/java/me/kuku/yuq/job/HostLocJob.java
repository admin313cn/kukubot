package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.logic.HostLocLogic;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JobCenter
public class HostLocJob {

	@Inject
	private HostLocService hostLocService;
	@Inject
	private HostLocLogic hostLocLogic;
	@Inject
	private GroupService groupService;
	@Inject
	private QqService qqService;

	private int locId = 0;

	@Cron("1m")
	public void locMonitor() throws IOException {
		List<Map<String, String>> list = hostLocLogic.post();
		if (list.size() == 0) return;
		List<Map<String, String>> newList = new ArrayList<>();
		if (locId != 0){
			for (Map<String, String> map : list) {
				if (Integer.parseInt(map.get("id")) <= locId) break;
				newList.add(map);
			}
		}
		locId = Integer.parseInt(list.get(0).get("id"));
		List<QqEntity> qqList = qqService.findByLocMonitor(true);
		List<GroupEntity> groupList = groupService.findByLocMonitor(true);
		for (Map<String, String> map : newList) {
			String str = "Loc有新帖了！！" + "\n" +
					"标题：" + map.get("title") + "\n" +
					"昵称：" + map.get("name") + "\n" +
					"链接：" + map.get("url");
			str += "\n内容：" + hostLocLogic.postContent(map.get("url"));
			for (QqEntity  qqEntity : qqList) {
				BotUtils.sendMessage(qqEntity, str);
			}
			for (GroupEntity groupEntity : groupList) {
				BotUtils.sendMessage(groupEntity.getGroup(), str);
			}
		}
	}

}
