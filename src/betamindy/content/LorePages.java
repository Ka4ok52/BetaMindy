package betamindy.content;

import arc.*;
import arc.files.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import betamindy.world.blocks.campaign.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static arc.Core.settings;

public class LorePages {
    //Id of each chapter. NEVER CHANGE THE ORDER OF CHAPTERS, ALWAYS APPEND TO END
    public static int lastID = -1;
    public static final IntMap<Chapter> idMap = new IntMap<>();
    public static Chapter
    esot0 = new Chapter("Introduction", new Page[]{
        makePage("esot/introduction"),
        makePage("esot/history"),
        makePage("esot/warning"),
    }),
    esot1 = new Chapter("LOG_SER_01", new Page[]{
        makePage("esot/geology_1")
    });

    public static class Chapter {
        public Page[] pages;
        public final int id;
        public boolean optional;
        public String icon = "betamindy-file-icon", name = "";

        public Chapter(String name, Page[] pages, boolean optional){
            id = ++lastID;
            this.name = name;
            this.pages = pages;
            this.optional = optional;
            idMap.put(id, this);
        }

        public Chapter(String name, Page[] pages){
            this(name, pages, false);
        }

        public boolean unlocked(){
            return settings.getBool(LoreManual.pageTag + id, false);
        }

        public void unlock(){
            settings.put(LoreManual.pageTag + id, true);
        }

        public TextureRegion getIcon(){
            return Core.atlas.find(icon, Icon.file == null ? Core.atlas.find("router") : Icon.file.getRegion());
        }
    }

    public static void addLocked(Table cont){
        cont.top().left();
        cont.defaults().center();
        cont.labelWrap("[accent]File missing or corrupted.[]").pad(5).growX();
        cont.row();
    }

    /** Makes a page. Go check out Goobrr/Esoterum!
     * @author Goober
     */
    public static class Page {
        Table cont, frame;
        Cons<Page> content;

        public Page(Cons<Page> con){
            content = con;
        }

        public void addContent(Table targetTable){
            cont = targetTable;
            frame = null;
            cont.top().left();
            cont.defaults().top().left();
            content.get(this);
        }

        public void row(){
            if(!tableMode) cont.row();
            else if(lineCol){
                cont.image().color(tableColor).fillY().growY().width(tableStroke).pad(0);
            }
        }

        public void addText(String content){
            if(tableMode) cont.add(content).pad(2);
            else cont.labelWrap(content).pad(5).grow();
            row();
        }

        public void addRawText(String content){
            cont.add(content).pad(2);
            row();
        }

        public void addHeader(String title, Color color){
            cont.labelWrap(title)
                    .color(color).fontScale(1.5f)
                    .padLeft(15).growX();
            cont.row();
            cont.image(Tex.whiteui)
                    .color(Pal.darkishGray).height(4)
                    .growX().pad(5);
            cont.row();
        }

        public void addHeader(String title){
            addHeader(title, Pal.accent);
        }

        public void addImage(String image, @Nullable String title){

            if(tableMode){
                cont.table(t -> {
                    t.image(Core.atlas.find(image));
                }).pad(5);
            }
            else{
                if(title != null){
                    cont.labelWrap(title)
                            .color(Pal.darkishGray).fontScale(0.8f)
                            .padLeft(15).growX();
                    cont.row();
                }
                cont.table(Tex.button, t -> {
                    t.image(Core.atlas.find(image));
                }).pad(5).growX();
            }
            row();
        }

        public void addNote(String content, @Nullable String author){
            cont.table(Tex.button, t -> {
                t.labelWrap(content)
                        .top().left()
                        .padLeft(5).growX();
                if(author != null){
                    t.row();
                    t.labelWrap("- " + author)
                            .fontScale(0.8f)
                            .top().right()
                            .padRight(5).growX()
                            .get().setAlignment(Align.right);
                }
            }).pad(5).growX();
            row();
        }

        public void startTable(@Nullable String title){
            frame = cont;
            cont = new Table();
            if(alighLeft) cont.defaults().pad(2).left();
            else cont.defaults().pad(2).center();
            if(title != null){
                cont.image().color(tableColor).fillX().growX().height(tableStroke).colspan(tableColspan).pad(0);
                cont.row();
                cont.add(title).fillX().growX().colspan(lineCol ? tableColspan - 1 : tableColspan).center();
                if(lineCol){
                    cont.image().color(tableColor).fillY().growY().width(tableStroke).pad(0);
                }
                cont.row();
            }
            cont.image().color(tableColor).fillX().growX().height(tableStroke).colspan(tableColspan).pad(0);
            cont.row();
        }

        public void addRowImage(){
            cont.row();
            cont.image().color(tableColor).fillX().growX().height(tableStroke).colspan(tableColspan).pad(0);
            cont.row();
        }

        public void endTable(){
            if(frame != null){
                frame.table(table -> {
                    table.image().color(tableColor).fillY().growY().width(tableStroke).pad(0);
                    table.add(cont).growX();
                    if(!lineCol) table.image().color(tableColor).fillY().growY().width(tableStroke).pad(0);
                }).pad(7).growX();
                cont = frame;
                cont.row();
            }
        }
    }

    // make a ManualPage from a file in assets/pages/
    // will return a blank page if an error occurs
    public static Page makePage(String pageName){
        return buildPage(getPageFile(pageName));
    }

    // returns a file in the pages/ asset directory as a string array of lines separated by two newlines (\n\n).
    public static String[] getPageFile(String name){
        Fi file = Vars.tree.get("pages/" + name);

        if (!file.exists()) {
            Log.info("Failed to load " + name);
            return new String[]{"[accent]File missing or corrupted.[]"};
        }

        return file.readString().split("\\r?\\n\\r?\\n");
    }

    static boolean tableMode, lineCol, alighLeft;
    static Color tableColor = Pal.lightishGray;
    static float tableStroke = 4f;
    static int tableColspan = 1;
    static String div = "";

    // build a ManualPage from an array of strings
    public static Page buildPage(String[] contents){
        Log.info("Lines: "+contents.length);
        tableMode = false;
        return new Page(t -> {
            for(int i = 0; i < contents.length; i++){
                String line = contents[i];
                Log.info("L->"+line);
                // if the line starts with [h], parse it as a header element
                if(line.startsWith("[h]")){
                    t.addHeader(line.substring(3));
                    continue;
                }

                // if the line starts with [i], parse it as an image element
                if(line.startsWith("[i]")){
                    String[] tmp = line.substring(3).split("\\[t]");
                    t.addImage(tmp[0], tmp.length == 2 ? tmp[1] : null);
                    continue;
                }

                // if the line starts with [n], parse it as a "personal" note
                if(line.startsWith("[n]")){
                    String[] tmp = line.substring(3).split("\\[t]");
                    t.addNote(tmp[0], tmp.length == 2 ? tmp[1] : null);
                    continue;
                }

                // if the line starts with [tb], start table mode
                if(line.startsWith("[tb]") && !tableMode){
                    tableMode = true;
                    tableColor = Pal.gray;
                    tableStroke = 4f;
                    tableColspan = 1;
                    lineCol = alighLeft = false;
                    div = "";
                    String title = null;

                    String[] tmp = line.substring(4).split("\\[");
                    for(String s : tmp){
                        int e = s.lastIndexOf("]");
                        if(e <= 0) continue;
                        s = s.substring(0, e);
                        if(s.startsWith("c:#")){
                            try{
                                tableColor = Color.valueOf(Tmp.c2, s.substring(3));
                            }
                            catch(Exception ignored){}
                        }
                        else if(s.startsWith("c:")){
                            try{
                                tableColor = Color.valueOf(Tmp.c2, s.substring(2));
                            }
                            catch(Exception ignored){}
                        }
                        else if(s.startsWith("s:")){
                            try{
                                tableStroke = Float.parseFloat(s.substring(2));
                                if(tableStroke < 0){
                                    tableStroke = 0.01f;
                                    tableColor = Color.clear;
                                }
                            }
                            catch(Exception ignored){}
                        }
                        else if(s.startsWith("div:")){
                            div = s.substring(4);
                        }
                        else if(s.startsWith("t:")){
                            title = s.substring(2);
                        }
                        else if(s.equals("col")){
                            lineCol = true;
                        }
                        else if(s.equals("left")){
                            alighLeft = true;
                        }
                    }


                    //fast-forward to the end of table to get the max colspan
                    int csum = 0;
                    for(int j = i + 1; j < contents.length; j++){
                        if(div.equals("")){
                            if(contents[j].startsWith("[/tb]")){
                                if(csum > tableColspan) tableColspan = csum;
                                break;
                            }
                            //newlines are divs
                            if(contents[j].startsWith("-")){
                                if(csum > tableColspan) tableColspan = csum;
                                csum = 0;
                            }
                            else csum++;
                        }
                        else{
                            if(contents[j].startsWith("[/tb]")){
                                break;
                            }
                            csum = contents[j].split(div).length;
                            if(csum > tableColspan) tableColspan = csum;
                        }
                    }
                    if(lineCol) tableColspan *= 2;
                    Log.info("Div>" + div);
                    Log.info("Col>" + tableColspan);

                    t.startTable(title);
                    continue;
                }

                if(tableMode){
                    if(line.startsWith("[/tb]")){
                        tableMode = false;
                        t.endTable();
                        continue;
                    }
                    else if(div.equals("")){
                        //endlines are divs
                        if(line.startsWith("-")){
                            t.addRowImage();
                            continue;
                        }
                    }
                    else{
                        String[] tmp = line.split(div);
                        for(String s : tmp){
                            t.addRawText(s);
                        }
                        t.addRowImage();
                        continue;
                    }
                }



                // else, parse it as normal text
                t.addText(line);
            }
        });
    }
}