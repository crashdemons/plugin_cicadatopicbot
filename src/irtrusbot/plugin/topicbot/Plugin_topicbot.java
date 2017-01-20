/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irtrusbot.plugin.topicbot;
import irtrusbot.*;
import java.util.ArrayList;
import java.util.Arrays;
/**
 *
 * @author crash
 */
public class Plugin_topicbot extends IrcPlugin {

    private int timer_list=0;
    private ArrayList<String> admins = new ArrayList<String>();
    private ArrayList<String> temp = new ArrayList<String>();
    
    public Plugin_topicbot () { //constructor - set plugin information and defaults.
        name="topicbot";
        version="1.0";
        description="Cicadasolvers topic and moderation bot.";
        defaults.setProperty("topic","");
    }
    
    @Override
    public IrcEventAction handleEvent(IrcEvent event){//handle an event notification from the bot and tell the bot what to do with it.
        switch(event.type){
            case BOT_START://any startup routines needed
                break;
            case BOT_STOP://any shutdown routines needed.
                saveConfig();
                break;
            case COMMAND:
            {
                if(event.command.type.equals("332")){
                    String chan=event.command.parameters.get(event.command.parameters.size() -2 );
                    if(!chan.equalsIgnoreCase("#cicadasolvers")) break;
                    String topic=event.command.parameters.get(event.command.parameters.size() -1 );
                    if(topic.contains("[!]")) break;
                    config.setProperty("topic", topic);
                    saveConfig();
                }
                if(event.command.type.equals("353")){
                    String chan=event.command.parameters.get(event.command.parameters.size() -2 );
                    if(!chan.equalsIgnoreCase("#cicadasolvers")) break;
                    String users=event.command.parameters.get(event.command.parameters.size() -1 );
                    String[] ausers=users.split("\\s+");
                    for(String user : ausers){
                        user=user.replace("+","");
                        if(user.startsWith("@")) temp.add(user.replace("@", ""));
                    }
                }
                if(event.command.type.equals("366")){
                    String chan=event.command.parameters.get(event.command.parameters.size() -2 );
                    if(!chan.equalsIgnoreCase("#cicadasolvers")) break;
                    admins=new ArrayList<>(temp);
                    temp.clear();
                }
                break;
            }
            case TICK:
                timer_list+=1;
                if(timer_list==60){
                    timer_list=0;
                    requestTopic("#cicadasolvers");
                    requestList("#cicadasolvers");
                }
                break;
            case CHAT://PRIVMSG's being received or sent (channel and private messages)
            {
                if(event.direction==IrcDirection.SENDING) break;//ignore outgoing messages.
                IrcMessage im = event.message;//gives the to, from, text part
                if(im.from.nick.toUpperCase().endsWith("BOT")) break;//ignore posts from bots.
                if(im.text.startsWith("~helloworld")){//check if post was a command
                    IrcMessage reply = im.getReply(session.account,"Hello World!",false);//reply from our account, to channel or PM, not directly to the sender.
                    postMessage(reply);//queue reply for sending.
                }
                if(im.text.startsWith("~admins")){
                    String output="List of admins: ";
                    for(String admin : admins) output+=admin+" ";
                    IrcMessage reply = im.getReply(session.account,output,true);//reply from our account, to channel or PM, not directly to the sender.
                    postMessage(reply);//queue reply for sending.
                }
                if(im.text.startsWith("~m")){
                    String output="Result: ";
                    if(admins.contains(im.from.nick)){
                        output+=" Authorized user, executing.";
                        postCommand(new IrcCommand("MODE #cicadasolvers +m"));
                        postCommand(new IrcCommand("TOPIC #cicadasolvers :[!] liber work in progress, pm op for voice"));
                    }else{
                        output+=" Unauthorized user.";
                    }
                    IrcMessage reply = im.getReply(session.account,output,false);//reply from our account, to channel or PM, not directly to the sender.
                    postMessage(reply);//queue reply for sending.
                }
                if(im.text.startsWith("~dm")){
                    String output="Result: ";
                    if(admins.contains(im.from.nick)){
                        output+=" Authorized user, executing.";
                        postCommand(new IrcCommand("MODE #cicadasolvers -m"));
                        postCommand(new IrcCommand("TOPIC #cicadasolvers :"+config.getProperty("topic")));
                    }else{
                        output+=" Unauthorized user.";
                    }
                    IrcMessage reply = im.getReply(session.account,output,false);//reply from our account, to channel or PM, not directly to the sender.
                    postMessage(reply);//queue reply for sending.
                }
            }
            break;
        }
        return IrcEventAction.CONTINUE;//don't block this event from being sent to other plugins.
    }
    
    public void requestTopic(String channel){
        IrcCommand ic = new IrcCommand("TOPIC "+channel);
        postCommand(ic);
    }
    public void requestList(String channel){
        IrcCommand ic = new IrcCommand("NAMES "+channel);
        postCommand(ic);
    }
    public void postMessage(IrcMessage im){
        IrcCommand ic = new IrcCommand(im.toOutgoing());//convert IM to IRC Command tokens
        IrcEvent event = new IrcEvent(IrcEventType.COMMAND,IrcState.UNDEFINED,IrcState.UNDEFINED,ic);//make an event notification to send this message
        event.direction=IrcDirection.SENDING;//outgoing
        postEvent(event);//notify the bot/plugin manager of event
    }
    public void postCommand(IrcCommand ic){
        IrcEvent event = new IrcEvent(IrcEventType.COMMAND,IrcState.UNDEFINED,IrcState.UNDEFINED,ic);//make an event notification to send this message
        event.direction=IrcDirection.SENDING;//outgoing
        postEvent(event);//notify the bot/plugin manager of event
    }
    
    
    
    
    public static void main(String[] args) {
        // unused
    }
    
}
