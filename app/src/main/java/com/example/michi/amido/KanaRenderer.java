package com.example.michi.amido;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by michi on 12.01.16.
 */
public class KanaRenderer {
    class Syllable {
        public String kana;
        public String roma;
        public Syllable(String kana, String roma) {
            this.kana = kana;
            this.roma = roma;
        }
    }
    static ArrayList<Syllable> syllables;

    private static KanaRenderer instance;
    public static KanaRenderer getInstance() {
        if (instance == null)
            instance = new KanaRenderer();
        return instance;
    }

    private KanaRenderer() {
        syllables = new ArrayList<>();
        add("った", "tta");
        add("って", "tte");
        add("っと", "tto");
        add("っぱ", "ppa");
        add("っぴ", "ppi");
        add("っぷ", "ppu");
        add("っぺ", "ppe");
        add("っぽ", "ppo");
        add("きゃ", "kya");
        add("きゅ", "kyu");
        add("きょ", "kyo");
        add("しゃ", "sha");
        add("しゅ", "shu");
        add("しょ", "sho");
        add("ちゃ", "cha");
        add("ちゅ", "chu");
        add("ちょ", "cho");
        add("にゃ", "nya");
        add("にゅ", "nyu");
        add("にょ", "nyo");
        add("ひゃ", "hya");
        add("ひゅ", "hyu");
        add("ひょ", "hyo");
        add("みゃ", "mya");
        add("みゅ", "myu");
        add("みょ", "myo");
        add("りゃ", "rya");
        add("りゅ", "ryu");
        add("りょ", "ryo");
        add("ぎゃ", "gya");
        add("ぎゅ", "gyu");
        add("ぎょ", "gyo");
        add("びゃ", "bya");
        add("びゅ", "byu");
        add("びょ", "byo");
        add("ぴゃ", "pya");
        add("ぴゅ", "pyu");
        add("ぴょ", "pyo");
        add("が", "ga");
        add("ぎ", "gi");
        add("ぐ", "gu");
        add("げ", "ge");
        add("ご", "go");
        add("ざ", "za");
        add("じ", "ji");
        add("ず", "zu");
        add("ぜ", "ze");
        add("ぞ", "zo");
        add("じゃ", "ja");
        add("じゅ", "ju");
        add("じょ", "jo");
        add("だ", "da");
        //add("づ", "zu???");
        add("で", "de");
        add("ど", "do");
        add("ば", "ba");
        add("び", "bi");
        add("ぶ", "bu");
        add("べ", "be");
        add("ぼ", "bo");
        add("ぱ", "pa");
        add("ぴ", "pi");
        add("ぷ", "pu");
        add("ぺ", "pe");
        add("ぽ", "po");
        add("か", "ka");
        add("き", "ki");
        add("く", "ku");
        add("け", "ke");
        add("こ", "ko");
        add("さ", "sa");
        add("し", "shi");
        add("す", "su");
        add("せ", "se");
        add("そ", "so");
        add("た", "ta");
        add("ち", "chi");
        add("つ", "tsu");
        add("て", "te");
        add("と", "to");
        add("な", "na");
        add("に", "ni");
        add("ぬ", "nu");
        add("ね", "ne");
        add("の", "no");
        add("は", "ha");
        add("ひ", "hi");
        add("ふ", "fu");
        add("へ", "he");
        add("ほ", "ho");
        add("ま", "ma");
        add("み", "mi");
        add("む", "mu");
        add("め", "me");
        add("も", "mo");
        add("や", "ya");
        add("ゆ", "yu");
        add("よ", "yo");
        add("ら", "ra");
        add("り", "ri");
        add("る", "ru");
        add("れ", "re");
        add("ろ", "ro");
        add("わ", "wa");
        add("を", "wo");
        add("あ", "a");
        add("い", "i");
        add("う", "u");
        add("え", "e");
        add("お", "o");
        add("ん", "n");

        add("ア", "A");
        add("イ", "I");
        add("ウ", "U");
        add("エ", "E");
        add("オ", "O");
        add("カ", "KA");
        add("キ", "KI");
        add("ク", "KU");
        add("ケ", "KE");
        add("コ", "KO");
        add("キャ", "KYA");
        add("キュ", "KYU");
        add("キョ", "KYO");
        add("サ", "SA");
        add("シ", "SHI");
        add("ス", "SU");
        add("セ", "SE");
        add("ソ", "SO");
        add("シャ", "SHA");
        add("シュ", "SHU");
        add("ショ", "SHO");
        add("タ", "TA");
        add("チ", "CHI");
        add("ツ", "TSU");
        add("テ", "TE");
        add("ト", "TO");
        add("チャ", "CHA");
        add("チュ", "CHU");
        add("チョ", "CHO");
        add("ナ", "NA");
        add("ニ", "NI");
        add("ヌ", "NU");
        add("ネ", "NE");
        add("ノ", "NO");
        add("ニャ", "NYA");
        add("ニュ", "NYU");
        add("ニョ", "NYO");
        add("ハ", "HA");
        add("ヒ", "HI");
        add("フ", "FU");
        add("ヘ", "HE");
        add("ホ", "HO");
        add("ヒャ", "HYA");
        add("ヒュ", "HYU");
        add("ヒョ", "HYO");
        add("マ", "MA");
        add("ミ", "MI");
        add("ム", "MU");
        add("メ", "ME");
        add("モ", "MO");
        add("ミャ", "MYA");
        add("ミュ", "MYU");
        add("ミョ", "MYO");
        add("ヤ", "YA");
        add("ユ", "YU");
        add("ヨ", "YO");
        add("ラ", "RA");
        add("リ", "RI");
        add("ル", "RU");
        add("レ", "RE");
        add("ロ", "RO");
        add("リャ", "RYA");
        add("リュ", "RYU");
        add("リョ", "RYO");
        add("ワ", "WA");
        add("ヲ", "WO");
        add("ン", "N");
        add("ガ", "GA");
        add("ギ", "GI");
        add("グ", "GU");
        add("ゲ", "GE");
        add("ゴ", "GO");
        add("ギャ", "GYA");
        add("ギュ", "GYU");
        add("ギョ", "GYO");
        add("ザ", "ZA");
        add("ジ", "JI");
        add("ズ", "ZU");
        add("ゼ", "ZE");
        add("ゾ", "ZO");
        /*add("ジャ", "JA");
        add("ジュ", "JU");
        add("ジョ", "JO");*/
        add("ダ", "DA");
        add("ヂ", "JI");
        //add("ヅ", "ZU");
        add("デ", "DE");
        add("ド", "DO");
        add("ヂャ", "JA");
        add("ヂュ", "JU");
        add("ヂョ", "JO");
        add("バ", "BA");
        add("ビ", "BI");
        add("ブ", "BU");
        add("ベ", "BE");
        add("ボ", "BO");
        add("ビャ", "BYA");
        add("ビュ", "BYU");
        add("ビョ", "BYO");
        add("パ", "PA");
        add("ピ", "PI");
        add("プ", "PU");
        add("ペ", "PE");
        add("ポ", "PO");
        add("ピャ", "PYA");
        add("ピュ", "PYU");
        add("ピョ", "PYO");

        sort();
    }

    private void add(String kana, String roma) {
        syllables.add(new Syllable(kana, roma));
    }

    private void sort() {
        for (int i=0; i<syllables.size(); i++)
            for (int j=i+1; j<syllables.size(); j++)
                if (syllables.get(i).roma.length() < syllables.get(j).roma.length()) {
                    Syllable t = syllables.get(i);
                    syllables.set(i, syllables.get(j));
                    syllables.set(j, t);
                }
    }

    public static String render(String in) {
        getInstance();
        StringBuilder out = new StringBuilder();
        int pos = 0;
        while (pos < in.length()) {
            boolean found = false;
            for (Syllable s : syllables) {
                if (s.roma.length() > in.length() - pos)
                    continue;
                if (in.substring(pos, pos + s.roma.length()).equals(s.roma)) {
                    out.append(s.kana);
                    pos += s.roma.length();
                    found = true;
                    break;
                }
            }
            if (!found) {
                out.append(in.charAt(pos));
                pos++;
            }
        }
        return out.toString();
    }
}
