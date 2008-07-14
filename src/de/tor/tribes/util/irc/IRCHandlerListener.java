/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.irc;

import jerklib.events.AwayEvent;
import jerklib.events.ChannelListEvent;
import jerklib.events.ConnectionCompleteEvent;
import jerklib.events.ConnectionLostEvent;
import jerklib.events.IRCEvent;
import jerklib.events.InviteEvent;
import jerklib.events.JoinCompleteEvent;
import jerklib.events.KickEvent;
import jerklib.events.MessageEvent;
import jerklib.events.MotdEvent;
import jerklib.events.NickChangeEvent;
import jerklib.events.NickInUseEvent;
import jerklib.events.NickListEvent;
import jerklib.events.NoticeEvent;
import jerklib.events.PartEvent;
import jerklib.events.QuitEvent;
import jerklib.events.ServerInformationEvent;
import jerklib.events.ServerVersionEvent;
import jerklib.events.TopicEvent;
import jerklib.events.WhoEvent;
import jerklib.events.WhoisEvent;
import jerklib.events.WhowasEvent;
import jerklib.events.modes.ModeEvent;

/**
 *
 * @author Jejkal
 */
public interface IRCHandlerListener {

    public void fireConnectedEvent(ConnectionCompleteEvent event);

    public void fireChannelMessageEvent(MessageEvent event);

    public void fireChannelJoinEvent(JoinCompleteEvent event);

    public void fireAwayEvent(AwayEvent event);

    public void fireChannelListEvent(ChannelListEvent event);

    public void fireConnectionLostEvent(ConnectionLostEvent event);

    public void fireInviteEvent(InviteEvent event);

    public void fireKickEvent(KickEvent event);

    public void fireModeEvent(ModeEvent event);

    public void fireNickChangeEvent(NickChangeEvent event);

    public void fireMotdEvent(MotdEvent event);

    public void fireNickInUseEvent(NickInUseEvent event);

    public void fireNickListEvent(NickListEvent event);

    public void fireNoticeEvent(NoticeEvent event);

    public void firePartEvent(PartEvent event);

    public void firePrivateMessageEvent(MessageEvent event);

    public void fireQuitEvent(QuitEvent event);

    public void fireServerInformationEvent(ServerInformationEvent event);

    public void fireServerVersionEvent(ServerVersionEvent event);

    public void fireTopicEvent(TopicEvent event);

    public void fireWhoisEvent(WhoisEvent event);

    public void fireWhowasEvent(WhowasEvent event);

    public void fireWhoEvent(WhoEvent event);

    public void fireIRCEvent(IRCEvent event);
}
