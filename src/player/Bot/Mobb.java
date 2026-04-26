/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package player.Bot;

import java.util.Random;
import mob.Mob;
import services.PlayerService;
import services.SkillService;
import utils.Util;

/**
 *
 * @author Bùi Công Hoan
 */
public class Mobb {
     private Mob mAttack;
     
     public long lastTimeChanM;
     
     public Bot bot;
     
     
     public Mobb(Bot b){
         this.bot = b;
     }
     
     public void update(){
        this.Attack();
        this.chanGeMap();
     }
     
     public void GetMobAttack(){
         if(this.bot.zone.mobs.size() >= 1){
            if(this.mAttack == null || this.mAttack.isDie()){
            mAttack = this.bot.zone.mobs.get(new Random().nextInt(this.bot.zone.mobs.size()));
         }
      }
    }
     
     
     public void Attack(){
         this.GetMobAttack();
             if(Util.isTrue(50 , 100)){
                this.bot.playerSkill.skillSelect = this.bot.playerSkill.skills.get(0);
             } else {
                this.bot.playerSkill.skillSelect = this.bot.playerSkill.skills.get(1);
             }
       if(this.mAttack != null){
           if(this.bot.UseLastTimeSkill()){
               PlayerService.gI().playerMove(this.bot, this.mAttack.location.x, this.mAttack.location.y);
               SkillService.gI().useSkill(this.bot, null, this.mAttack, -1, null);
           }
      }
    }
    
        
   public void chanGeMap(){
       if(this.lastTimeChanM < ((System.currentTimeMillis() - 150000) -  new Random().nextInt(150000))){
           this.bot.joinMap();
       }
   }
    
}