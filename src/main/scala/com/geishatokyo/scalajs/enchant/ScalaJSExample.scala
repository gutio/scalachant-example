package com.geishatokyo.scalajs.enchant
import scala.scalajs.js
import js.JSApp
import js.annotation.JSExport
import js.Dynamic.{ global => g }
import com.geishatokyo.scalajs.enchant._

import scala.util.Random

object ScalaJSExample extends JSApp {
  val enemyBaseSpeed = 8
  val startLife = 5

  def lifeLabel(): Label = {
    var life = new Label()
    life.x = 400 - 64
    life.y = 20
    life.color = "blue"
    life.font = "32px Meiryo"
    return life
  }

  def pointLabel(): Label = {
    var point = new Label()
    point.x = 400 - 380
    point.y = 20
    point.color = "green"
    point.font = "32px Meiryo"
    return point
  }

  def setupPlayer(image: Surface): Sprite = {
    var player = new Sprite(32, 64)
    player.image = image
    player.x = 200 - 32
    player.y = 300
    player.frame = js.Array(0)
    return player
  }

  def setupEnemy(image: Surface): Sprite = {
    var enemy = new Sprite(64, 64)
    enemy.image = image
    enemy.x = 200 - 32
    enemy.y = 0
    enemy.frame = js.Array(0)
    return enemy
  }

  def setupEnd(image: Surface): Sprite = {
    var end = new Sprite(189, 97)
    end.image = image
    end.x = 100
    end.y = 150
    end.frame = js.Array(0)
    return end
  }

  def setupAtack(image: Surface): Sprite = {
    var atack = new Sprite(1, 16)
    atack.image = image
    atack.x = 0
    atack.y = 0
    atack.frame = js.Array(0)
    return atack
  }

  def surface(game: Core, image: String): Surface = {
    return game.assets.asInstanceOf[js.Dictionary[Surface]](image)
  }

  def main(): Unit = {
    enchant()

    val playerImage = "images/space0.png"
    val enemyImage = "images/space1.png"

    // 終了画像追加
    val endImage = "images/end.png"

    // 弾画像
    val atackImage = "images/bar.png"

    // ランダム用変数のインスタンス化
    val rand = new Random

    val game = new Core(400, 400)
    //　利用する画像の文字列をここに追加
    game.preload(js.Array(playerImage, enemyImage, endImage, atackImage))
    game.fps = 20

    game.onload = {
      () => 
        var life = this.lifeLabel
        var point = this.pointLabel
        var timeCounter = new Counter(0)
        var lifeCounter = new Counter(startLife)
        var pointCounter = new Counter(0)
        life.text = lifeCounter.count.toString
        point.text = "Point: " + pointCounter.count.toString
        var player = this.setupPlayer(this.surface(game, playerImage))
        var enemyList = List(this.setupEnemy(this.surface(game, enemyImage)))
        var atack = this.setupAtack(this.surface(game, atackImage))
        var end = this.setupEnd(this.surface(game, endImage))
        game.rootScene.addChild(player)
        enemyList.foreach(enemy => game.rootScene.addChild(enemy))
        game.rootScene.addChild(life)
        game.rootScene.addChild(point)
        game.rootScene.addChild(atack) 
        
        // key bind
        game.keybind(32, "space");

        player.addEventListener("enterframe", {
          e:Event =>
            // game restart
            if (timeCounter.count > 0) {
              timeCounter.decrement()
            } else if(lifeCounter.count > 0) {
              player.visible = true
              enemyList.foreach(enemy => enemy.visible = true)
            }

            // move
            if (game.input.left){
              player.x -= 10
            }
            if (game.input.right){
              player.x += 10
            }
            if (game.input.up){
                player.y -= 10
            }
            if (game.input.down){
                player.y += 10
            }

            // 弾移動
            if (atack.visible == true){
              atack.y -= 8
              if (atack.y < 0 - 16)
                atack.visible = false
            }

            // 弾発射
            if (game.input.space && atack.visible == false){
                atack.x = player.x + 16
                atack.y = player.y
                atack.visible = true
            }

            // enemy move
            if (player.visible){
              enemyList.foreach{enemy => 
                val enemySpeed: Int = enemyBaseSpeed + 
                  (enemyBaseSpeed * (pointCounter.count / (10.0))).toInt
                
                enemy.y += (enemySpeed match {
                  case x if x < 20 => x
                  case _ => 20
                })
                
                if (enemy.y > 400){
                  enemy.y = 0 - rand.nextInt(100)
                  enemy.x = 200 - rand.nextInt(200)
                }
              }
            }

            // atack action
            enemyList.foreach{enemy => 
              if (atack.visible == true && atack.intersect(enemy)){
                atack.visible = false
                enemy.y = 0 - rand.nextInt(100)
                enemy.x = (400 - 32) - rand.nextInt(400 -32)
                pointCounter.increment()
                point.text = "Point: " + pointCounter.count.toString
                if(pointCounter.count % 10 == 0 && pointCounter.count <= 100) {
                  var newEnemy = this.setupEnemy(this.surface(game, enemyImage))
                  enemyList = newEnemy :: enemyList
                  game.rootScene.addChild(newEnemy)
                }
              }
            }

            // crash action
            if (! enemyList.forall(enemy => ! player.intersect(enemy))) {
                // player reset
                player.visible = false
                // 場所リセット
                player.x = 200 - 32
                player.y = 300
                
                // enemy reset
                enemyList.foreach{enemy =>
                  enemy.visible = false
                  // 出現位置ランダム化
                  enemy.x = 400 - rand.nextInt(400)
                  enemy.y = 50 - rand.nextInt(50)
                }

                // 得点などの後処理
                lifeCounter.decrement()
                if(lifeCounter.count == 0){
                  life.x = 200
                  life.text = "＼(^o^)／"
                  game.rootScene.addChild(end)
                  end.visible = true
                }
                else {
                  life.text = lifeCounter.count.toString
                  timeCounter.increment(10)
                }

                atack.visible = false
            }

            js.Object
        })

    }

    game.start()
  }
}
