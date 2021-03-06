package dev.mlnr.spidey.commands.moderation;

import dev.mlnr.spidey.Spidey;
import dev.mlnr.spidey.cache.GuildSettingsCache;
import dev.mlnr.spidey.objects.I18n;
import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import dev.mlnr.spidey.utils.Emojis;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.mlnr.spidey.utils.Utils.addReaction;
import static dev.mlnr.spidey.utils.Utils.deleteMessage;

@SuppressWarnings({"unused", "StringBufferReplaceableByString"})
public class PurgeCommand extends Command
{
    public PurgeCommand()
    {
        super("purge", new String[]{"d", "delete"}, Category.MODERATION, Permission.MESSAGE_MANAGE, 2, 6);
    }

    @Override
    public void execute(String[] args, CommandContext ctx)
    {
        var guild = ctx.getGuild();
        var i18n = ctx.getI18n();
        if (!guild.getSelfMember().hasPermission(ctx.getTextChannel(), getRequiredPermission(), Permission.MESSAGE_HISTORY))
        {
            ctx.replyError(i18n.get("commands.purge.other.messages.failure.no_perms"));
            return;
        }
        if (args.length == 0)
        {
            ctx.replyError(i18n.get("commands.purge.other.messages.failure.wrong_syntax", GuildSettingsCache.getPrefix(guild.getIdLong())));
            return;
        }
        ctx.getArgumentAsUnsignedInt(0, amount ->
        {
            if (amount < 1 || amount > 100)
            {
                ctx.replyError(i18n.get("number.range", 100));
                return;
            }
            if (args.length == 1)
            {
                respond(ctx, null, amount);
                return;
            }
            ctx.getArgumentAsUser(1, user -> respond(ctx, user, amount));
        });
    }

    private void respond(CommandContext ctx, User target, int limit)
    {
        var message = ctx.getMessage();
        var channel = ctx.getTextChannel();
        var i18n = ctx.getI18n();
        message.delete().queue(ignored -> channel.getIterableHistory().cache(false).limit(target == null ? limit : 100).queue(messages ->
        {
            if (messages.isEmpty())
            {
                ctx.replyError(i18n.get("commands.purge.other.messages.failure.no_messages.text"));
                return;
            }
            var msgs = target == null ? messages : messages.stream().filter(msg -> msg.getAuthor().equals(target)).limit(limit).collect(Collectors.toList());
            if (msgs.isEmpty())
            {
                ctx.replyError(i18n.get("commands.purge.other.messages.failure.no_messages.user", target.getAsTag()));
                return;
            }
            var pinned = msgs.stream().filter(Message::isPinned).collect(Collectors.toList());
            if (pinned.isEmpty())
            {
                proceed(msgs, target, ctx);
                return;
            }
            var size = pinned.size();
            var builder = new StringBuilder(size == 1 ? i18n.get("commands.purge.other.messages.pinned.one")
                    : i18n.get("commands.purge.other.messages.pinned.multiple"));
            builder.append(" ").append(i18n.get("commands.purge.other.messages.pinned.confirmation.text")).append(" ");
            builder.append(size == 1 ? i18n.get("commands.purge.other.messages.pinned.confirmation.one")
                    : i18n.get("commands.purge.other.messages.pinned.confirmation.multiple"));
            builder.append("? ").append(i18n.get("commands.purge.other.messages.pinned.middle_text.text"));
            builder.append(" ").append(size == 1 ? i18n.get("commands.purge.other.messages.pinned.middle_text.one")
                    : i18n.get("commands.purge.other.messages.pinned.middle_text_multiple"));
            builder.append(i18n.get("commands.purge.other.messages.pinned.end_text"));

            channel.sendMessage(builder.toString()).queue(sentMessage ->
            {
                addReaction(sentMessage, Emojis.CHECK);
                addReaction(sentMessage, Emojis.WASTEBASKET);
                addReaction(sentMessage, Emojis.CROSS);

                Spidey.getWaiter().waitForEvent(GuildMessageReactionAddEvent.class,
                        ev -> ev.getUser().equals(ctx.getAuthor()) && ev.getMessageIdLong() == sentMessage.getIdLong(),
                        ev ->
                        {
                            switch (ev.getReactionEmote().getName())
                            {
                                case Emojis.CHECK:
                                    deleteMessage(sentMessage);
                                    break;
                                case Emojis.CROSS:
                                    deleteMessage(sentMessage);
                                    return;
                                case Emojis.WASTEBASKET:
                                    msgs.removeAll(pinned);
                                    deleteMessage(sentMessage);
                                    if (msgs.isEmpty())
                                    {
                                        ctx.replyError(i18n.get("commands.purge.other.messages.failure.no_messages.unpinned"));
                                        return;
                                    }
                                    break;
                                default:
                            }
                            proceed(msgs, target, ctx);
                        }, 1, TimeUnit.MINUTES, () ->
                        {
                            deleteMessage(sentMessage);
                            ctx.replyError(i18n.get("took_too_long"));
                        });
            });
        }, throwable -> ctx.replyError(i18n.get("internal_error", "purge messages", throwable.getMessage()))));
    }

    private void proceed(List<Message> toDelete, User user, CommandContext ctx)
    {
        var channel = ctx.getTextChannel();
        var future = CompletableFuture.allOf(channel.purgeMessages(toDelete).toArray(new CompletableFuture[0]));
        future.whenCompleteAsync((ignored, throwable) ->
        {
            var i18n = ctx.getI18n();
            if (throwable != null)
            {
                ctx.replyError(i18n.get("internal_error", "purge messages", throwable.getMessage()));
                return;
            }
            channel.sendMessage(generateSuccessMessage(toDelete.size(), user, i18n))
                    .delay(Duration.ofSeconds(5))
                    .flatMap(Message::delete)
                    .queue();
        });
    }

    private String generateSuccessMessage(int amount, User user, I18n i18n)
    {
        return i18n.get("commands.purge.other.messages.success.text", amount) + " "
                + (amount == 1 ? i18n.get("commands.purge.other.messages.success.one") : i18n.get("commands.purge.other.messages.success.multiple"))
                + (user == null ? "." : " " + i18n.get("commands.purge.other.messages.success.user", user) + ".");
    }
}