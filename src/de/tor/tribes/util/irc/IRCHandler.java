/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tor.tribes.util.irc;

import jerklib.ConnectionManager;
import jerklib.Profile;
import jerklib.Session;
import jerklib.events.AwayEvent;
import jerklib.events.ChannelListEvent;
import jerklib.events.ConnectionCompleteEvent;
import jerklib.events.ConnectionLostEvent;
import jerklib.events.IRCEvent;
import jerklib.events.IRCEvent.Type;
import jerklib.events.InviteEvent;
import jerklib.events.JoinCompleteEvent;
import jerklib.events.JoinEvent;
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
import jerklib.listeners.IRCEventListener;

/**
 *
 * @author Jejkal
 */
public class IRCHandler implements IRCEventListener {

    private IRCHandlerListener mListener = null;
    private ConnectionManager mConnectionManager = null;
    private Session mCurrentSession = null;
    private boolean connected = false;

    public IRCHandler(Profile pProfile, IRCHandlerListener pListener) {
        mListener = pListener;
        mConnectionManager = new ConnectionManager(pProfile);
    }

    public void connect(String pServer) {
        mCurrentSession = mConnectionManager.requestConnection((pServer == null) ? "irc.freenode.net" : pServer);
        mCurrentSession.addIRCEventListener(this);
    }

    public void disconnect(String pMessage) {
        if (mCurrentSession != null) {
            mCurrentSession.close(pMessage);
            connected = false;
        }
    }

    public boolean isConnected() {
        return mCurrentSession.isConnected();
    }

    public void sayPrivate(String pUser, String pMessage) {
        mCurrentSession.sayPrivate(pUser, pMessage);
    }

    public void sayRaw(String pMessage) {
        mCurrentSession.sayRaw(pMessage);
    }

    public void joinChannel(String pChannel) {
        if (!isConnected()) {
            return;
        }
        if (pChannel == null) {
            return;
        }
        if (mCurrentSession == null) {
            return;
        } else {
            if (!pChannel.startsWith("#")) {
                pChannel = "#" + pChannel;
            }
            System.out.println("Joining " + pChannel);
            mCurrentSession.join(pChannel);
        }
    }

    @Override
    public void receiveEvent(IRCEvent e) {

        if (e.getType() == Type.CONNECT_COMPLETE) {
            mCurrentSession = ((ConnectionCompleteEvent) e).getSession();
            mListener.fireConnectedEvent(((ConnectionCompleteEvent) e));
        } else if (e.getType() == Type.CHANNEL_MESSAGE) {
            mListener.fireChannelMessageEvent((MessageEvent) e);
        } else if (e.getType() == Type.JOIN_COMPLETE) {
            mListener.fireChannelJoinEvent((JoinCompleteEvent) e);
        } else if (e.getType() == Type.JOIN) {
            mListener.fireJoinEvent((JoinEvent) e);
        } else if (e.getType() == Type.AWAY_EVENT) {
            mListener.fireAwayEvent((AwayEvent) e);
        } else if (e.getType() == Type.CHANNEL_LIST_EVENT) {
            mListener.fireChannelListEvent((ChannelListEvent) e);
        } else if (e.getType() == Type.CONNECTION_LOST) {
            mListener.fireConnectionLostEvent((ConnectionLostEvent) e);
        } else if (e.getType() == Type.INVITE_EVENT) {
            mListener.fireInviteEvent((InviteEvent) e);
        } else if (e.getType() == Type.KICK_EVENT) {
            mListener.fireKickEvent((KickEvent) e);
        } else if (e.getType() == Type.MODE_EVENT) {
            mListener.fireModeEvent((ModeEvent) e);
        } else if (e.getType() == Type.NICK_CHANGE) {
            mListener.fireNickChangeEvent((NickChangeEvent) e);
        } else if (e.getType() == Type.MOTD) {
            mListener.fireMotdEvent((MotdEvent) e);
        } else if (e.getType() == Type.NICK_IN_USE) {
            mListener.fireNickInUseEvent((NickInUseEvent) e);
        } else if (e.getType() == Type.NICK_LIST_EVENT) {
            mListener.fireNickListEvent((NickListEvent) e);
        } else if (e.getType() == Type.NOTICE) {
            mListener.fireNoticeEvent((NoticeEvent) e);
        } else if (e.getType() == Type.PART) {
            mListener.firePartEvent((PartEvent) e);
        } else if (e.getType() == Type.PRIVATE_MESSAGE) {
            mListener.firePrivateMessageEvent((MessageEvent) e);
        } else if (e.getType() == Type.QUIT) {
            mListener.fireQuitEvent((QuitEvent) e);
        } else if (e.getType() == Type.SERVER_INFORMATION) {
            mListener.fireServerInformationEvent((ServerInformationEvent) e);
        } else if (e.getType() == Type.SERVER_VERSION_EVENT) {
            mListener.fireServerVersionEvent((ServerVersionEvent) e);
        } else if (e.getType() == Type.SERVER_VERSION_EVENT) {
            mListener.fireServerVersionEvent((ServerVersionEvent) e);
        } else if (e.getType() == Type.TOPIC) {
            mListener.fireTopicEvent((TopicEvent) e);
        } else if (e.getType() == Type.WHOIS_EVENT) {
            mListener.fireWhoisEvent((WhoisEvent) e);
        } else if (e.getType() == Type.WHOWAS_EVENT) {
            mListener.fireWhowasEvent((WhowasEvent) e);
        } else if (e.getType() == Type.WHO_EVENT) {
            mListener.fireWhoEvent((WhoEvent) e);
        } else {
            mListener.fireIRCEvent(e);

        }
    }
}
