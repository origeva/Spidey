package dev.mlnr.spidey.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.mlnr.spidey.objects.command.Category;
import dev.mlnr.spidey.objects.command.Command;
import dev.mlnr.spidey.objects.command.CommandContext;
import dev.mlnr.spidey.objects.music.AudioLoader;
import dev.mlnr.spidey.utils.Emojis;
import dev.mlnr.spidey.utils.MusicUtils;
import dev.mlnr.spidey.utils.StringUtils;
import net.dv8tion.jda.api.Permission;

@SuppressWarnings("unused")
public class SearchCommand extends Command
{
    public SearchCommand()
    {
        super("search", new String[]{"ytsearch"}, Category.MUSIC, Permission.UNKNOWN, 1, 4);
    }

    @Override
    public void execute(String[] args, CommandContext ctx)
    {
        var musicPlayer = MusicUtils.checkQueryInput(args, ctx);
        if (musicPlayer == null)
            return;
        var i18n = ctx.getI18n();
        MusicUtils.loadQuery(musicPlayer, "ytsearch:" + args[0], new AudioLoadResultHandler()
        {
            @Override
            public void trackLoaded(AudioTrack track) {}

            @Override
            public void playlistLoaded(AudioPlaylist playlist)
            {
                var selectionEmbedBuilder = MusicUtils.createMusicResponseBuilder();
                selectionEmbedBuilder.setAuthor(i18n.get("commands.search.other.searching", args[0]));

                var tracks = playlist.getTracks();
                StringUtils.createSelection(selectionEmbedBuilder, tracks, ctx, "track", MusicUtils::formatTrack, choice ->
                {
                    var url = tracks.get(choice).getInfo().uri;
                    var loader = new AudioLoader(musicPlayer, url, ctx);
                    MusicUtils.loadQuery(musicPlayer, url, loader);
                });
            }

            @Override
            public void noMatches()
            {
                ctx.replyError(i18n.get("music.messages.failure.no_matches", args[0]), Emojis.DISLIKE);
            }

            @Override
            public void loadFailed(FriendlyException exception)
            {
                ctx.replyError(i18n.get("commands.search.other.error"), Emojis.DISLIKE);
            }
        });
    }
}