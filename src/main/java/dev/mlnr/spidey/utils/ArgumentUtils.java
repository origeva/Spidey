package dev.mlnr.spidey.utils;

import dev.mlnr.spidey.objects.command.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArgumentUtils
{
    private static final Pattern ID_REGEX = Pattern.compile("(\\d{17,18})");

    private ArgumentUtils() {}

    public static void parseArgumentAsInt(String argument, CommandContext ctx, IntConsumer consumer)
    {
        try
        {
            consumer.accept(Integer.parseInt(argument));
        }
        catch (NumberFormatException ex)
        {
            ctx.replyError(ctx.getI18n().get("number.invalid"));
        }
    }

    public static void parseArgumentAsUnsignedInt(String argument, CommandContext ctx, IntConsumer consumer)
    {
        try
        {
            consumer.accept(Integer.parseUnsignedInt(argument));
        }
        catch (NumberFormatException ex)
        {
            ctx.replyError(ctx.getI18n().get("number.invalid"));
        }
    }

    public static void parseArgumentAsTextChannel(String argument, CommandContext ctx, Consumer<TextChannel> consumer)
    {
        parseArgumentAsMentionable(argument, ctx, mentionable -> consumer.accept(((TextChannel) mentionable)), Message.MentionType.CHANNEL);
    }

    public static void parseArgumentAsRole(String argument, CommandContext ctx, Consumer<Role> consumer)
    {
        parseArgumentAsMentionable(argument, ctx, mentionable -> consumer.accept(((Role) mentionable)), Message.MentionType.ROLE);
    }

    public static void parseArgumentAsUser(String argument, CommandContext ctx, Consumer<User> consumer)
    {
        parseArgumentAsMentionable(argument, ctx, mentionable -> consumer.accept(((User) mentionable)), Message.MentionType.USER);
    }

    private static void parseArgumentAsMentionable(String argument, CommandContext ctx, Consumer<IMentionable> consumer, Message.MentionType type)
    {
        var message = ctx.getMessage();
        var guild = ctx.getGuild();
        var author = ctx.getAuthor();
        var typeName = type.name().toLowerCase();
        var i18n = ctx.getI18n();
        var notFound = i18n.get("argument_parser.not_found." + typeName) + " " + i18n.get("argument_parser.not_found.text");
        var embedBuilder = Utils.createEmbedBuilder(author);

        if (type.getPattern().matcher(argument).matches())
        {
            var mentionable = message.getMentions(type).get(0);
            consumer.accept(mentionable);
            return;
        }

        var idMatcher = ID_REGEX.matcher(argument);
        if (idMatcher.matches())
        {
            var mentionableId = Long.parseLong(idMatcher.group());
            if (type == Message.MentionType.USER)
            {
                if (mentionableId == author.getIdLong())
                {
                    consumer.accept(author);
                    return;
                }
                var jda = ctx.getJDA();
                var selfUser = jda.getSelfUser();
                if (mentionableId == selfUser.getIdLong())
                {
                    consumer.accept(selfUser);
                    return;
                }
                jda.retrieveUserById(mentionableId).queue(consumer, failure -> ctx.replyError(notFound));
                return;
            }
            var mentionable = type == Message.MentionType.CHANNEL ? guild.getTextChannelById(mentionableId) : guild.getRoleById(mentionableId);
            if (mentionable == null)
            {
                ctx.replyError(notFound);
                return;
            }
            consumer.accept(mentionable);
            return;
        }

        if (argument.length() >= 2 && argument.length() <= 32)
        {
            if (type == Message.MentionType.USER)
            {
                if (argument.equalsIgnoreCase(ctx.getMember().getEffectiveName()))
                {
                    consumer.accept(author);
                    return;
                }
                message.getGuild().retrieveMembersByPrefix(argument, 10)
                    .onSuccess(members ->
                    {
                        if (members.isEmpty())
                        {
                            ctx.replyError(notFound);
                            return;
                        }
                        if (members.size() == 1)
                        {
                            consumer.accept(members.get(0).getUser());
                            return;
                        }
                        createMentionableSelection(embedBuilder, members.stream().map(Member::getUser).collect(Collectors.toList()), ctx, "user",
                                mentionable -> mentionable.getAsMention() + " - **" + ((User) mentionable).getAsTag() + "**", choice -> consumer.accept(members.get(choice).getUser()));
                    });
                return;
            }
            var mentionables = type == Message.MentionType.CHANNEL ? guild.getTextChannelsByName(argument, true) : guild.getRolesByName(argument, true);
            if (mentionables.isEmpty())
            {
                ctx.replyError(i18n.get("argument_parser.not_found.name", i18n.get("argument_parser.not_found." + typeName).toLowerCase()));
                return;
            }
            if (mentionables.size() == 1)
            {
                consumer.accept(mentionables.get(0));
                return;
            }
            createMentionableSelection(embedBuilder, mentionables, ctx, typeName, mentionable -> mentionable.getAsMention() + " - ID: " + mentionable.getIdLong(),
                    choice -> consumer.accept(mentionables.get(choice)));
            return;
        }
        ctx.replyError(notFound);
    }

    private static void createMentionableSelection(EmbedBuilder selectionBuilder, List<? extends IMentionable> mentionables, CommandContext ctx, String type,
                                                  Function<IMentionable, String> mapper, IntConsumer choiceConsumer)
    {
        StringUtils.createSelection(selectionBuilder, mentionables, ctx, type, mapper::apply, choiceConsumer);
    }
}