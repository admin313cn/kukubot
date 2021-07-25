package me.kuku.simbot.interceptor;

import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.component.mirai.message.event.MiraiTempMsg;
import love.forte.simbot.constant.PriorityConstant;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ListenerContext;
import love.forte.simbot.listener.MsgInterceptContext;
import love.forte.simbot.listener.MsgInterceptor;
import love.forte.simbot.listener.ScopeContext;
import me.kuku.simbot.controller.ContextSession;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqService;
import me.kuku.simbot.utils.BotUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeforeInterceptor implements MsgInterceptor {

	@Autowired
	private QqService qqService;

	@NotNull
	@Override
	@Transactional
	public InterceptionType intercept(@NotNull MsgInterceptContext context) {
		ScopeContext scopeContext = context.getListenerContext().getContext(ListenerContext.Scope.EVENT_INSTANT);
		MsgGet msgGet = context.getMsgGet();
		ContextSession contextSession = new ContextSession(msgGet);
		scopeContext.set("session", contextSession);
		long qq = msgGet.getAccountInfo().getAccountCodeNumber();
		QqEntity qqEntity = qqService.findByQq(qq);
		if (qqEntity != null) {
			scopeContext.set("qq", qqEntity.getQq());
			scopeContext.set("qqEntity", qqEntity);
			if (msgGet instanceof GroupMsg) {
				GroupMsg groupMsg = (GroupMsg) msgGet;
				long group = groupMsg.getGroupInfo().getGroupCodeNumber();
				GroupEntity groupEntity = qqEntity.getGroup(group);
				scopeContext.set("group", groupEntity.getGroup());
				scopeContext.set("groupEntity", groupEntity);
				Long at = BotUtils.getAt(groupMsg.getMsgContent());
				if (at != null) scopeContext.set("at", at);
			} else if (msgGet instanceof MiraiTempMsg) {
				long group = ((MiraiTempMsg) msgGet).getGroupInfo().getGroupCodeNumber();
				GroupEntity groupEntity = qqEntity.getGroup(group);
				scopeContext.set("group", groupEntity.getGroup());
				scopeContext.set("groupEntity", groupEntity);
			}
		}
		return InterceptionType.PASS;
	}

	@Override
	public int getPriority() {
		return PriorityConstant.SECOND;
	}
}
