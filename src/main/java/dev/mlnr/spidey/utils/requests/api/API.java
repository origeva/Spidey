package dev.mlnr.spidey.utils.requests.api;

public enum API
{
    KSOFT_NSFW("https://api.ksoft.si/images/rand-reddit/%s?span=month", "Bearer " + System.getenv("ksoft")),
    TOP_GG("https://top.gg/api/bots/772446532560486410", System.getenv("topgg")),
    URBAN_DICTIONARY("http://api.urbandictionary.com/v0/define?term=%s", null);

    private final String url;
    private final String key;

    API(final String url, final String key)
    {
        this.url = url;
        this.key = key;
    }

    public String getUrl()
    {
        return this.url;
    }

    public String getKey()
    {
        return this.key;
    }

    public enum Stats
    {
        TOP_GG("https://top.gg/api/bots/%s/stats", "server_count", System.getenv("topgg"));

        private final String url;
        private final String statsParam;
        private final String key;

        Stats(final String url, final String statsParam, final String key)
        {
            this.url = url;
            this.statsParam = statsParam;
            this.key = key;
        }

        public String getUrl()
        {
            return this.url;
        }

        public String getStatsParam()
        {
            return this.statsParam;
        }

        public String getKey()
        {
            return this.key;
        }
    }
}