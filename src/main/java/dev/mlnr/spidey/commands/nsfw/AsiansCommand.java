package dev.mlnr.spidey.commands.nsfw;

import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;

@SuppressWarnings("unused")
public class AsiansCommand extends Command
{
    public AsiansCommand()
    {
        super("asiansgonewild", new String[]{"asian", "asians"}, Category.NSFW, Permission.UNKNOWN, 0, 4);
    }

    @Override
    public void execute(String[] args, CommandContext ctx) {}
}