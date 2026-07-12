package services;

/*
 *
 *
 * @author CongHoan
 */

import models.Template.FlagBag;
import java.util.List;
import item.Item;
import player.Player;
import server.Manager;
import network.Message;
import java.util.ArrayList;

public class FlagBagService {

    private final List<FlagBag> flagClan = new ArrayList<>();
    private static FlagBagService i;
    private static final int[] FLAG_BAG_EFFECT_COLLISION_IDS = {172, 218, 219, 220, 221, 222, 223, 224, 225, 228};
    private static final int[] FLAG_BAG_CLIENT_ALIAS_IDS = {118, 119, 120, 121, 122, 123, 124, 125};

    public static FlagBagService gI() {
        if (i == null) {
            i = new FlagBagService();
        }
        return i;
    }

    public void sendIconFlagChoose(Player player, int id) {
        int clientId = toUnsignedByteId(id);
        FlagBag fb = getFlagBag(toServerFlagBagId(clientId));
        if (fb != null) {
            Message msg;
            try {
                msg = new Message(-62);
                msg.writer().writeByte(toClientFlagBagId(fb.id));
                msg.writer().writeByte(fb.iconEffect.length + 1);
                msg.writer().writeShort(fb.iconId);
                for (Short iconId : fb.iconEffect) {
                    msg.writer().writeShort(iconId);
                }
                msg.writer().writeShort(toClientFlagBagId(fb.id));
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    public void sendIconEffectFlag(Player player, int id) {
        int clientId = toUnsignedByteId(id);
        FlagBag fb = getFlagBag(toServerFlagBagId(clientId));
        if (fb != null) {
            Message msg;
            try {
                msg = new Message(-63);
                msg.writer().writeByte(toClientFlagBagId(fb.id));
                msg.writer().writeByte(fb.iconEffect.length);
                for (Short iconId : fb.iconEffect) {
                    msg.writer().writeShort(iconId);
                }
                msg.writer().writeShort(toClientFlagBagId(fb.id));
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    public void sendListFlagClan(Player pl) {
        List<FlagBag> list = getFlagsForChooseClan();
        Message msg;
        try {
            msg = new Message(-46);
            msg.writer().writeByte(1); // type
            msg.writer().writeByte(list.size());
            for (FlagBag fb : list) {
                msg.writer().writeByte(toClientFlagBagId(fb.id));
                msg.writer().writeUTF(fb.name);
                msg.writer().writeInt(fb.gold);
                msg.writer().writeInt(fb.gem);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public FlagBag getFlagBag(int id) {
        for (FlagBag fb : Manager.FLAGS_BAGS) {
            if (fb.id == id) {
                return fb;
            }
        }
        return null;
    }

    public int toClientFlagBagId(int id) {
        int aliasId = getAliasIdForRawFlagBagId(id);
        if (aliasId != -1) {
            return aliasId;
        }
        return id;
    }

    public int toServerFlagBagId(int id) {
        for (int rawId : FLAG_BAG_EFFECT_COLLISION_IDS) {
            int aliasId = getAliasIdForRawFlagBagId(rawId);
            if (aliasId == id) {
                return rawId;
            }
        }
        return id;
    }

    public boolean isClientAlias(int rawId, int clientId) {
        return rawId != clientId;
    }

    private int toUnsignedByteId(int id) {
        return id & 0xFF;
    }

    public short resolveEquippedFlagBagId(Item item) {
        if (item == null || !item.isNotNullItem() || item.template == null || item.template.type != 11) {
            return -1;
        }
        int rawId = item.template.part;
        FlagBag byPart = rawId >= 0 ? getFlagBag(rawId) : null;
        if (byPart != null && byPart.iconId == item.template.iconID) {
            return (short) rawId;
        }
        if (item.template.iconID > 0) {
            FlagBag byIcon = getFlagBagByIconId(item.template.iconID);
            if (byIcon != null) {
                return (short) byIcon.id;
            }
        }
        if (rawId >= 0 && getFlagBag(rawId) != null) {
            return (short) rawId;
        }
        return -1;
    }

    private FlagBag getFlagBagByIconId(short iconId) {
        for (FlagBag fb : Manager.FLAGS_BAGS) {
            if (fb.iconId == iconId) {
                return fb;
            }
        }
        return null;
    }

    private int getAliasIdForRawFlagBagId(int rawId) {
        int collisionIndex = getCollisionIndex(rawId);
        if (collisionIndex < 0 || getFlagBag(rawId) == null) {
            return -1;
        }
        int aliasIndex = 0;
        for (int i = 0; i < FLAG_BAG_EFFECT_COLLISION_IDS.length; i++) {
            int collisionRawId = FLAG_BAG_EFFECT_COLLISION_IDS[i];
            if (getFlagBag(collisionRawId) == null) {
                continue;
            }
            int aliasId = getAvailableAliasId(aliasIndex++);
            if (i == collisionIndex) {
                return aliasId;
            }
        }
        return -1;
    }

    private int getCollisionIndex(int rawId) {
        for (int i = 0; i < FLAG_BAG_EFFECT_COLLISION_IDS.length; i++) {
            if (FLAG_BAG_EFFECT_COLLISION_IDS[i] == rawId) {
                return i;
            }
        }
        return -1;
    }

    private int getAvailableAliasId(int aliasIndex) {
        int skipped = 0;
        for (int aliasId : FLAG_BAG_CLIENT_ALIAS_IDS) {
            if (getFlagBag(aliasId) != null) {
                continue;
            }
            if (skipped++ == aliasIndex) {
                return aliasId;
            }
        }
        return -1;
    }

    public List<FlagBag> getFlagsForChooseClan() {
        if (flagClan.isEmpty()) {
            int[] flagsId = { 0, 8, 7, 6, 5, 4, 3, 2, 1, 18, 17, 16, 15, 14, 13,
                    12, 11, 10, 9, 27, 26, 25, 24, 23, 36, 32, 33, 34, 35, 19, 22, 21, 20, 29
                    // , 37, 38, 69, 70, 71, 77, 78, 79
            };
            for (int i = 0; i < flagsId.length; i++) {
                flagClan.add(getFlagBag(flagsId[i]));
            }
        }
        return flagClan;
    }
}
