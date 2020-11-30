package succ.util;


//Wrapper for emojis
public class Emoji {


    private long guildid;
    private long sourceGuildid;
    private String roleid;
    private String emoji;
    private boolean unicode;

    public Emoji(long guildid, long sourceGuildid,  String roleid, String emoji, boolean unicode){
        this.guildid = guildid;
        this.sourceGuildid = sourceGuildid;
        this.roleid = roleid;
        this.emoji = emoji;
        this.unicode = unicode;
    }

    public Emoji(long sourceGuildid, String emoji, boolean unicode){
        this.sourceGuildid = sourceGuildid;
        this.emoji = emoji;
        this.unicode = unicode;
    }

    public Emoji(String emoji, boolean unicode){
        this.sourceGuildid = sourceGuildid;
        this.roleid = roleid;
        this.emoji = emoji;
        this.unicode = unicode;
    }

    public long getServerId(){
        return guildid;
    }

    public long getSourceId(){
        return sourceGuildid;
    }

    public String getRoleId(){
        return roleid;
    }

    public String getEmoji(){
        return emoji;
    }

    public boolean isUnicode(){
        return unicode;
    }

    public String toString(){
        return "Emoji: guildid: "+guildid+", roleid: "+roleid+", emojiid: "+emoji+", unicode: "+unicode;
    }
}
