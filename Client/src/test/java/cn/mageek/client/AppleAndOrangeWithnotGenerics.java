package cn.mageek.client;

/**
 * @author Mageek Chiu
 * @date 2018/5/20 0020:19:50
 */

import java.util.*;

class Snow {}

class Powder extends Snow {}
class Light extends  Powder {}
class Heavy extends Powder {}
class Crusty extends Snow {}
class Slush extends  Snow {}

public class AppleAndOrangeWithnotGenerics {

    public static void main(String[] args) {

        List<Snow> snow1 = Arrays.asList(
                new Crusty(), new Slush(), new Powder()
        );

        List<Snow> snow2 = Arrays.asList(
                new Light(), new Heavy()
        );

        List<Snow> snow3 = new ArrayList<Snow>();
        Collections.addAll(snow3, new Light(), new Heavy());

        List<Snow> snow4 = Arrays.<Snow>asList(new Light(), new Heavy());
    }
}
