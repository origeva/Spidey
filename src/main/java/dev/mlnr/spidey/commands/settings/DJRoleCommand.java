package dev.mlnr.spidey.commands.settings;

import dev.mlnr.spidey.cache.GuildSettingsCache;
import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.util.EnumSet;

@SuppressWarnings("unused")
public class DJRoleCommand extends Command
{
    public DJRoleCommand()
    {
        super("djrole", new String[]{}, Category.SETTINGS, Permission.MANAGE_SERVER, 1, 4);
    }

    @Override
    public void execute(String[] args, CommandContext ctx)
    {
        var guildId = ctx.getGuild().getIdLong();
        var dbRole = GuildSettingsCache.getDJRoleId(guildId);
        var i18n = ctx.getI18n();
        if (args.length == 0)
        {
            if (dbRole == 0)
            {
                ctx.replyError(i18n.get("roles.not_set", "DJ"));
                return;
            }
            GuildSettingsCache.removeDJRole(guildId);
            ctx.reply(i18n.get("roles.removed", "DJ"));
            return;
        }
        ctx.getArgumentAsRole(0, role ->
        {
            var roleId = role.getIdLong();
            if (roleId == dbRole)
            {
                GuildSettingsCache.removeDJRole(guildId);
                ctx.reply(i18n.get("roles.reset", "DJ"));
                return;
            }
            if (!ctx.getMember().canInteract(role))
            {
                ctx.replyError(i18n.get("roles.cant_interact", "DJ"));
                return;
            }
            GuildSettingsCache.setDJRoleId(guildId, roleId);
            ctx.reply(i18n.get("roles.set", "DJ", role.getAsMention()), EnumSet.complementOf(EnumSet.of(Message.MentionType.ROLE)));
        });
    }
}