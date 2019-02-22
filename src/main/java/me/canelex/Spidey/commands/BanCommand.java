package me.canelex.Spidey.commands;

import me.canelex.Spidey.objects.command.Command;
import me.canelex.Spidey.utils.API;
import me.canelex.Spidey.utils.PermissionError;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class BanCommand implements Command {

	@Override
	public boolean called(GuildMessageReceivedEvent e) {
		
		return true;
		
	}

	@Override
	public void action(GuildMessageReceivedEvent e) {
		
		final String neededPerm = "BAN_MEMBERS";    		
		
		if (!e.getMessage().getContentRaw().equals("s!ban")) {
			
			if (e.getMember().hasPermission(Permission.valueOf(neededPerm))) {
				
        		String id = e.getMessage().getContentRaw().substring(6);
        		id = id.substring(0, id.indexOf(" "));
        		
        		String reason = e.getMessage().getContentRaw().substring((7 + id.length()));
        		
				e.getGuild().getController().ban(id, 0, reason).queue();   				    				
				
			}
			
			else {
				
				API.sendMessage(e.getChannel(), PermissionError.getErrorMessage(neededPerm), false);    				
				
			}    			
			
		}		
		
	}

	@Override
	public String help() {
		
		return "Bans user";
		
	}

	@Override
	public void executed(boolean success, GuildMessageReceivedEvent e) {
		
		return;
		
	}

}