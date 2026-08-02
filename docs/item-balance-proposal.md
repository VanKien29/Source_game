# Thong ke va de xuat can bang vat pham

Tai lieu nay luu lai phuong an can bang theo data hien tai. Ban SQL rieng da duoc chen khoi `BALANCE PATCH 2026-08-02` de import.

## Quy uoc option

- `50`: suc danh phan tram.
- `77`: HP phan tram.
- `103`: KI phan tram.
- `5`: sat thuong chi mang.
- `14`: chi mang goc.
- `94`: giam sat thuong.
- `101`: tiem nang suc manh.
- `30`: khoa giao dich, giu theo logic hien tai.

Khong bat buoc moi vat pham chi co 3 dong. Vat pham cao cap co the co 5-6 dong, nhung khi them option phu thi phai giam cac option chinh.

## Shop Cumber - tab Cai Trang

Shop hien tai la `shop_id = 23`, tab `Cai Trang`, `item_spec = 1705`.

| ID | Gia hien tai | Chi so de xuat |
|---:|---:|---|
| 1088 | 200 xu | 15 SD% + 15 HP% + 15 KI% |
| 1236 | 950 xu | 17 SD% + 18 HP% + 18 KI% + 3% chi mang goc |
| 1255 | 1.050 xu | 19 SD% + 18 HP% + 18 KI% + option 160 |
| 1274 | 1.300 xu | 20 SD% + 21 HP% + 21 KI% + 5% sat thuong chi mang + 3% chi mang goc |
| 1275 | 1.350 xu | 19 SD% + 22 HP% + 22 KI% + 5% giam sat thuong + 10% TNSM |
| 1424 | 2.200 xu | 21 SD% + 20 HP% + 20 KI% + 6% sat thuong chi mang + 4% chi mang goc + 3% giam sat thuong |
| 1436 | 2.200 xu | 20 SD% + 23 HP% + 21 KI% + 6% giam sat thuong + 10% TNSM + option 108 |
| 1437 | 2.200 xu | 20 SD% + 20 HP% + 24 KI% + 15% TNSM + 5% sat thuong chi mang + option 160 |

## Moc nap - chi tiet cai trang

Bang `moc_nap` co nhieu loai vat pham. Chi hai ID duoi day la cai trang va duoc can bang trong pham vi nay.

| Moc nap | ID | Chi so de xuat |
|---:|---:|---|
| 100.000 dong | 1632 | 15 SD% + 15 HP% + 15 KI% + 10% TNSM + 3% chi mang goc |
| 2.000.000 dong | 1805 | 32 SD% + 30 HP% + 30 KI% + 8% sat thuong chi mang + 5% chi mang goc + 5% giam sat thuong |

Chua can bang cac ID sau vi khong phai cai trang:

- `1699`: Bồ cào 9 răng.
- `1363`: thú cưỡi Con Cat hồng.
- `1668`: Pet Capybara hồng.
- `1502`: vũ khí Thanh Long Yển Nguyệt đao.
- `1578`: Cân Đẩu Vân.

## Shop Cumber - tab Pet

Pet duoc de chi so thap hon cai trang de tranh viec 3 nhom trang bi cong don qua manh.

| ID | Gia | Chi so de xuat |
|---:|---:|---|
| 1414 | 100 xu | 2 SD% + 2 HP% + 2 KI% |
| 1452 | 500 xu | 3 SD% + 4 HP% + 4 KI% |
| 1550 | 800 xu | 4 SD% + 5 HP% + 5 KI% |
| 1551 | 900 xu | 5 SD% + 6 HP% + 6 KI% + 2% chi mang goc |
| 1622 | 1.500 xu | 6 SD% + 8 HP% + 9 KI% + 3% sat thuong chi mang + option 108 |

## Shop Cumber - tab Deo Lung

Deo lung uu tien phong thu va option phu, khong day SD qua cao.

| ID | Gia | Chi so de xuat |
|---:|---:|---|
| 1572 | 200 xu | 3 SD% + 3 HP% + 3 KI% |
| 1574 | 500 xu | 4 SD% + 4 HP% + 4 KI% |
| 1577 | 800 xu | 5 SD% + 6 HP% + 6 KI% |
| 1669 | 1.200 xu | 5 SD% + 5 HP% + 5 KI% + 3% sat thuong chi mang |
| 1670 | 1.200 xu | 4 SD% + 7 HP% + 8 KI% + 3% giam sat thuong |
| 1699 | 1.700 xu | 6 SD% + 9 HP% + 10 KI% + 5% sat thuong chi mang + 4% giam sat thuong + option 108 |

## Shop Cumber - tab Van Bay

Van bay la nhom tien ich co chi so chien dau thap nhat trong ba nhom.

| ID | Gia | Chi so de xuat |
|---:|---:|---|
| 1541 | 100 xu | 1 SD% + 1 HP% + 1 KI% |
| 1554 | 250 xu | 2 SD% + 2 HP% + 2 KI% |
| 1563 | 550 xu | 3 SD% + 4 HP% + 4 KI% |
| 1578 | 800 xu | 4 SD% + 6 HP% + 6 KI% |
| 1603 | 1.200 xu | 5 SD% + 8 HP% + 9 KI% + 3% sat thuong chi mang + option 108 |

## Thu tu suc manh

```text
Van bay Cumber < Pet Cumber < Deo lung Cumber < Cai trang Santa < Cai trang Cumber < Cai trang moc nap cao
```

Day la thu tu theo vai tro, khong phai theo gia tien tuyet doi. Xu `1705` trong data hien tai la tien shop Cumber; chua tu them gia thoi vang vao cac dong nay.

## Pham vi da ra soat

Ngoai shop Cumber, file SQL hien tai con cac nguon phat vat pham co chi so chien dau:

| Nguon | Shop/bang | Don vi nhan | Danh gia hien tai |
|---|---|---|---|
| Santa mien phi | `SANTA`, `SANTA_PHUKIEN` | ngoc xanh | Moc tham chieu thap nhat; can giu chi so thap va co HSD hop ly |
| Cumber | `SHOP_XU_KRAI` | xu `1705` | Grindable, nam tren Santa |
| Bardock | `BARDOCK_SHOP` | thoi vang `457` | Grindable cham hon Cumber; co the gan voi nhom cao cap cày |
| Mua truc tiep | `SHOP_VND` | goi nap | Cao hon shop cày nhung khong duoc vuot qua moc nap cung cap |
| Shop tich nap | `SHOP_QUY_LAO` | 1 diem / 1.000 VND | HSD ngau nhien, cho phep cao hon moc nap thuong mot bac |
| Moc nap | `moc_nap` | nguong nap | Can tang dan theo nguong, nhung khong de mot mon out hoan toan nguoi cày |
| Top moc | `moc_nap_top` | xep hang nap | Nhom canh tranh, co the cao nhat nhung phai co tran |
| Top nhiem vu/suc manh | `moc_nhiem_vu_top`, `moc_suc_manh_top` | xep hang free | Khong nen ngang top nap; uu tien danh hieu va tai nguyen |
| Diem danh | `DIEM_DANH` | dang nhap hang ngay | Dang co ID `1897` la outlier, can ha chi so |
| Diem san boss | `SHOP_DIEMSANBOSS` | diem boss | Chu yeu la vat lieu, khong can tang combat |
| Thoi vang/pho anh hoan | `SHOP_TV`, `PHO_ANH_HOAN` | thoi vang/vat lieu | Chi nen la vat pham phu, khong can chen vao nhom top |

Chi so trong cac bang duoi day la muc tieu de sua data. Chua thay doi SQL hoac code.

## Moc nap thuong - de xuat day du

Gia moc nap la so tien tich luy. Cac dong khong co vat pham combat giu nguyen, chi can kiem tra lai so luong vat lieu neu muon dieu chinh kinh te.

| Moc | ID combat | Loai | Chi so hien tai | Chi so de xuat | Uu tien |
|---:|---:|---|---|---|---|
| 20.000 | 1699 | deo lung | 7/7/7 SD-HP-KI + 20 TNSM + HSD 3 | 8/8/8 + 10 TNSM + HSD 3 | P2 |
| 100.000 | 1632 | cai trang | 25/25/25 + 25 TNSM | 18/18/18 + 10 TNSM + 3 crit goc | P1 |
| 200.000 | 1363 | thu cuoi | 7/7/7 + 10 crit goc | 9/9/9 + 3 crit goc | P2 |
| 500.000 | 1668 | pet | 15/15/15 + 20 TNSM | 13/13/13 + 15 TNSM | P1 |
| 1.000.000 | 1502 | vu khi | 15/17/17 + 30 TNSM + 10 crit goc | 18/19/19 + 20 TNSM + 4 crit goc | P1 |
| 1.500.000 | 1578 | van bay | 11/11/11 + 25 TNSM | 12/12/12 + 15 TNSM | P2 |
| 2.000.000 | 1805 | cai trang | 35/40/40 + 50 TNSM + nhieu option phu | 30/30/30 + 8 crit damage + 5 crit goc + 5 giam sat thuong | P0 |

Giai thich ID khong phai cai trang:

- `1699` la deo lung, khong dung chung muc tieu voi cai trang Cumber du chi trung ID trong mot so data khac.
- `1363` la thu cuoi, `1668` la pet, `1578` la van bay, `1502` la vu khi.
- Cac vat pham nay co the co 3-5 dong, nhung dong phu phai phuc vu dung vai tro. Khong cong dong phu de vuot tran tong suc manh.

## Moc nap top, top nhiem vu va top suc manh

Ba bang top dang lap lai cung mot mau: hang dau co 8-9 option va chi so 35-50, trong khi hang sau dung cung loai phan thuong. Nen tach ro hang 1, 2, 3 va cho hang 4-10 ve phan thuong tich luy.

### `moc_nap_top`

| Hang | Vat pham | Chi so de xuat |
|---:|---|---|
| 1 | ID `1762` | 32 SD% + 35 HP% + 35 KI% + 10 crit damage + 7 crit goc + 5 giam sat thuong |
| 2 | ID `1762` | 28 SD% + 30 HP% + 30 KI% + 8 crit damage + 5 crit goc + 4 giam sat thuong |
| 3 | ID `1762` | 24 SD% + 26 HP% + 26 KI% + 6 crit damage + 4 crit goc + 3 giam sat thuong |
| 4-10 | ID `1807`, `1633`, `1272` | 10-18 SD/HP/KI tuy vai tro, khong them crit damage lon |

Hang 1 top nap co the cao hon mon moc nap 2 trieu, nhung khong nen vuot qua 35/35/35 va khong nen co dong tang TNSM 40-50. Phan thuong hang 2-3 nen giu khoang cach 10-15% so voi hang tren.

### `moc_nhiem_vu_top` va `moc_suc_manh_top`

Day la top free, nen khuyen khich bang danh hieu, tai nguyen va ngoai hinh thay vi dua chi so ngang top nap.

| Hang | Chi so de xuat cho vat pham dau |
|---:|---|
| 1 | 28 SD% + 30 HP% + 30 KI% + 8 crit damage + 5 crit goc |
| 2 | 24 SD% + 26 HP% + 26 KI% + 6 crit damage + 3 crit goc |
| 3 | 20 SD% + 22 HP% + 22 KI% + 4 crit damage + 3 crit goc |
| 4-10 | Khong phat vat pham combat; tang vat lieu, xu, thoi vang va danh hieu |

Hai bang nay dang co cac dong `1761` 35/40/40 va nhieu option phu. Do la outlier so voi muc free, can ha ve dung bang tren.

## Shop Bardock - thoi vang

Shop `BARDOCK_SHOP` co 23 cai trang, 10 pet, 15 van bay va 15 deo lung. Gia hien tai khoang 11-40 thoi vang cho trang bi co combat. Day la nguon grindable nen dat tren Cumber mot bac, nhung khong vuot moc nap 500k-1m.

| Nhom | Chi so hien tai | Chi so de xuat |
|---|---|---|
| Cai trang gia 11-18 | khoang 17-23 SD/HP/KI, crit damage 5-10 | 15-18 SD/HP/KI, toi da 1 option phu 3-5 |
| Cai trang gia 19-40 | khoang 20-23 SD/HP/KI, crit damage 5-10 | 18-22 SD/HP/KI, toi da 2 option phu; tong crit damage toi da 7 |
| Pet | 6-9 SD/HP/KI + option 94-109 | 5-8 SD/HP/KI + 1 option vai tro 5-8 |
| Van bay | 7-9 SD/HP/KI + crit damage 5-10 | 4-8 SD/HP/KI + crit damage 3-7 |
| Deo lung | 2-4 SD/HP/KI + option 175/117 | 2-4 SD/HP/KI, giu option chuc nang 175/117 |

Khong xoa cac option chuc nang cua pet/van/deo lung. Chi ha cac option combat dang lam tong set cong don qua manh.

## Shop VND va shop tich nap

### `SHOP_VND`

Cac tab cai trang, deo lung va van bay dang co mau gan nhu 20/20/20 + option `249=2000`. De xuat dung mot tran chung cho toan shop:

- Cai trang goi thap: 18-21 SD/HP/KI, toi da 1 option phu.
- Cai trang goi trung: 20-24 SD/HP/KI, toi da 2 option phu.
- Deo lung/van bay: 8-14 SD/HP/KI, toi da 1 option phu.
- Khong de option `249` bi tinh nhu dong combat neu no chi la dieu kien goi/han su dung.
- Cac goi VND co the co ngoai hinh hiem, nhung chi so nen thap hon moc nap 2 trieu.

### `SHOP_QUY_LAO` - tab Shop Tich Nap

Shop nay tieu `coupon`, voi quy doi 1 diem cho moi 1.000 VND nap. Do vat pham co HSD ngau nhien va mot so mon co the mat sau khi het han, shop duoc xep cao hon moc nap thuong mot bac; tuy nhien chi cac ID cao diem moi vuot moc nap max, khong phai toan bo shop.

| Nhom ID | Chi so hien tai | Chi so sau khi sua |
|---|---|---|
| `1676` | 5/4/4 + crit damage 4 | 12/12/12 + crit damage 6 |
| `1680` | 7/3/3 + crit damage 5 | 15/15/15 + crit damage 8 + crit goc 4 |
| `1677`, `1891` | 4/13/14 + ne/DR | 10/16/16 + ne/DR 4-5 |
| `1937` | 10/5/7 + crit damage 5 + crit goc 4 | 16/12/14 + crit damage 7 + crit goc 5 |
| `1828`, `1894` | 10/10/10 va 6/8/15 | 18/18/18 va 10/14/18 |
| `1698` | 25/34/29 + DR8 + option 108 | 32/35/32 + DR10 + ne5 |
| `1947` | 30/26/26 + crit damage 12 + crit goc 10 | 35/31/31 + crit damage 15 + crit goc 10 |
| `1938` | 25/29/35 + crit goc 8 + option 96 | 32/35/35 + crit goc 10 + option 96=12 |

Tat ca cac item tren giu option `231` de server xu ly HSD/vinh vien theo logic hien tai. Nguyen tac cho shop nay: chi so co the cao hon moc nap max, doi lai nguoi choi ton diem va co rui ro HSD; khong de shop nay vuot qua nhom top nap hang 1.

### Kick VIP tai ToriBot

Kick VIP khong nam trong SQL. Gia va phan thuong duoc hardcode trong `src/services/func/UseItem.java`, duoc goi tu NPC `src/npc/npc_manifest/ToriBot.java`:

- VIP 1: 20.000 VND.
- VIP 2: 50.000 VND, cai trang `1504` random 20-25 SD/HP/KI.
- VIP 3: 150.000 VND, cai trang `742` random 25-29 SD/HP/KI va pet `1318` random 5-9 SD/HP/KI.

File SQL khong the sua cac item nay. Neu can can lai Kick VIP phai sua source Java va build lai server rieng.

## Diem danh, phu kien va cac shop doi khac

### `DIEM_DANH`

Day la nguon free hang ngay. ID `1897` hien tai 30/30/30 + option 95/96=15 la outlier. De xuat:

| ID | Chi so de xuat |
|---:|---|
| `1897` | 12/12/12 + option 95=8 + option 96=8 + HSD 3 |
| `1941` | 8/8/8 + crit damage 10 + HSD 7 |
| `1539` | 8/8/8 + HSD 7 |
| `1760` | 6/6/6 + HSD 3 |
| `1625` | 3/3/3 + HSD 3 |

### `SANTA_PHUKIEN`

Day la shop phu kien free, gia 1.000-5.000 ngoc. Giu vai tro trang tri va tien ich:

- Nhom co HSD: 5-10 SD/HP/KI, toi da 1 option phu.
- Nhom co giam sat thuong/crit: 8-12 tong chi so chinh, khong them ca crit damage va crit goc trong cung mon.
- Khong de vat pham free co 15/15/15 kem nhieu option phu, vi se vuot vai tro Santa co ban.

### `SHOP_TV`, `PHO_ANH_HOAN`, `SHOP_DIEMSANBOSS`, `SHOP_BANG`

| Nguon | De xuat |
|---|---|
| `SHOP_TV` | Giu cac vat lieu. ID `1771` 10 HP% + 5 giam sat thuong la muc chap nhan duoc, khong tang them crit. |
| `PHO_ANH_HOAN` | Giu la vat pham phu: ID `1836` 6 SD% + 2 crit goc; `1837` 10 HP% + 1 option phong thu; `1838` 12 KI%. |
| `SHOP_DIEMSANBOSS` | Chi doi vat lieu, xu va diem boss; khong them cai trang combat manh. |
| `SHOP_BANG` | Uu tien item chuc nang bang hoi, khong dung lam nguon chi so chien dau. |

## Cac nguon khong nam trong tab shop

| Nguon | Vi tri | Huong can bang |
|---|---|---|
| Item su kien/hardcode | `ItemService.java` | Dat tran 20-25 SD/HP/KI cho mon free; mon co TNSM 40-49 phai ha ve 15-25 hoac doi sang option trang tri. |
| Qua NPC | `CauVang.java`, `HoanSec.java`, `NpcFactory.java` | Khong de qua NPC co chi so cao hon moc nap; tach trang bi Huy Diet/Thien Su khoi nhom cai trang. |
| Gift code | `GiftCodeService.java` | Chi so nam trong DB gift code, can audit theo tung code; khong sua mau global trong code. |
| Qua chien bang Namek | `ClanNamekWarRewardConfig` | Dat reward vao DB, cap thuong rank 1-3 khong vuot top nap; bat buoc tao bang truoc khi reward. |
| Qua boss/su kien | `ShenronEvent`, cac model boss | Uu tien vat lieu, danh hieu va ngoai hinh; neu co combat item thi dung cap Santa/Cumber. |
| Nang cap/ep sao/hematite | cac model `Combine` | Day la phep nang cap item dang co, khong phat chi so tu do. Chi can gioi han tran option sau khi nang cap. |

## Tran tong suc manh de ap dung chung

De tranh viec 4 mon phu cong don thanh mot set vuot muc, dung cac tran sau khi tinh toan:

| Nhom | Tong SD% toi da tu mot mon | Dong phu toi da |
|---|---:|---|
| Santa free | 15 | 1 |
| Cumber grindable | 24 | 2-3 |
| Bardock grindable cao | 22 | 2 |
| Shop VND | 24 | 2 |
| Moc nap 100k-1m | 24 | 2 |
| Moc nap 2m | 30 | 3 |
| Top nap hang 1 | 35 | 4 |

Khong cong ca `TNSM`, crit damage, crit goc va giam sat thuong theo cung mot he so SD. Moi loai co vai tro khac nhau. Khi test thuc te, can ghi lai 3 moc: dame thuong, dame crit va thoi gian ha boss; neu chenh lech giua build free va build nap vuot khoang 25-35% thi ha option phu truoc, khong ha vat lieu nhan.

## Thu tu sua data de tranh sai lech

1. Sua outlier `moc_nap` ID `1632`, `1668`, `1805` va `DIEM_DANH` ID `1897`.
2. Sua ba bang top `moc_nap_top`, `moc_nhiem_vu_top`, `moc_suc_manh_top` theo hang 1/2/3.
3. Sua `SHOP_QUY_LAO` va `BARDOCK_SHOP` theo nhom, sau do kiem tra lai gia.
4. Sua `SHOP_VND`, `SANTA_PHUKIEN` va cac mon hardcode su kien.
5. Chay test mot tai khoan free, mot tai khoan chi dung Cumber/Bardock va mot tai khoan moc nap. Ghi lai dame/HP/KI sau khi thay doi.

## Trang thai

- Da doc va thong ke: `moc_nap`, `moc_nap_top`, `moc_nhiem_vu_top`, `moc_suc_manh_top`, `SANTA_PHUKIEN`, `BARDOCK_SHOP`, `SHOP_VND`, `SHOP_QUY_LAO`, `DIEM_DANH`, `SHOP_TV`, `PHO_ANH_HOAN`, `SHOP_DIEMSANBOSS`, `SHOP_BANG`.
- Da danh dau outlier va muc de xuat trong tai lieu nay.
- Da chen khoi `BALANCE PATCH 2026-08-02` vao file SQL rieng, bao gom Shop Tich Nap theo quy doi 1 diem/1.000 VND va HSD ngau nhien.
- Kick VIP tai ToriBot da xac dinh la phan thuong hardcode trong source; chua sua source Java trong lan cap nhat SQL nay.
- Truoc khi sua that can chot lai cac option chuc nang `108`, `160`, `249`, `95`, `96`, `117`, `175` dang duoc dung o client/server cua ban.
