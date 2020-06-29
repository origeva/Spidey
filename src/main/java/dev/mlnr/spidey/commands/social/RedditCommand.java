package dev.mlnr.spidey.commands.social;

import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.json.Reddit;
import dev.mlnr.spidey.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

@SuppressWarnings("unused")
public class RedditCommand extends Command
{
	public RedditCommand()
	{
		super("reddit", new String[]{}, "Shows you info about entered subreddit. For example `reddit PewdiepieSubmissions`.",
				"reddit <subreddit>", Category.SOCIAL, Permission.UNKNOWN, 0, 0);
	}

	@Override
	public final void execute(final String[] args, final Message message)
	{
		if (args.length == 0)
		{
			Utils.returnError("Please specify a subreddit", message);
			return;
		}
		final var subreddit = args[0];
		final var reddit = new Reddit(subreddit);
		if (!reddit.exists())
		{
			Utils.returnError("Subreddit not found", message);
			return;
		}
		final var eb = Utils.createEmbedBuilder(message.getAuthor());
		final var communityIcon = reddit.getCommunityIcon();
		final var comIcon = communityIcon.length() == 0 ? "https://up.mlnr.dev/reddit.png" : communityIcon;
		final var icon = reddit.getIcon();
		final var title = reddit.getTitle();
		final var desc = reddit.getDesc();
		final var subs = reddit.getSubs();
		final var active = reddit.getActive();
		eb.setAuthor("r/" + reddit.getName(), "https://reddit.com/r/" + subreddit, "https://up.mlnr.dev/reddit.png");
		eb.setThumbnail(icon.length() == 0 ? comIcon : icon);
		eb.setColor(16727832);
		eb.addField("Subscribers", (subs >= 1000 ? "**" + Utils.getCompactNumber(subs) + "** (**" + Utils.format(subs) + "**)" : "**" + subs + "**"), false);
		eb.addField("Active users", (active >= 1000 ? "**" + Utils.getCompactNumber(active) + "** (**" + Utils.format(active) + "**)" : "**" + active + "**"), false);
		eb.addField("Title", (title.length() == 0 ? "**None**" : title), false);
		eb.addField("Description", (desc.length() == 0 ? "**None**" : desc), false);
		eb.addField("NSFW", "**" + (reddit.isNsfw() ? "Yes" : "No") + "**", false);
		Utils.sendMessage(message.getTextChannel(), eb.build());
	}
}