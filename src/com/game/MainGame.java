package com.game;

import java.io.IOException;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.AnimatedSprite.IAnimationListener;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.Debug;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.KeyEvent;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.neogame.R;


public class MainGame extends BaseExample implements IOnMenuItemClickListener,SensorEventListener  {
	// ===========================================================
	// Constants
	// ===========================================================
	
	private static final int CAMERA_WIDTH = 854;
	private static final int CAMERA_HEIGHT = 480;
	protected static final int MENU_RESET = 0;
	protected static final int MENU_QUIT = MENU_RESET + 1;
	private static final int STAR_NUMBER = 150;
	private static final int BULLET_NUMBER = 50;
	private static final int BULLET_SPEED = 200;
	private static final int SHIP_WIDTH = 16;
	private static final int SHIP_HEIGHT = 32;
	
	// ===========================================================
	// Fields
	// ===========================================================

	protected Scene scene;
	private Camera mCamera;
	
	private SQLite helper;
	private static String[] FROM = { "_ID", "name", "value" };  
    private static String ORDER_BY = "value" + " DESC" ;
	
	//private Texture mFontTexture;
	//private Font mFont;
	private Font mKingdomOfHeartsFont;
	private Texture mKingdomOfHeartsFontTexture;
	
	private Texture mBulletTexture;
	private TiledTextureRegion mBulletTextureRegion;
	
	private Texture mExplodeTexture;
	private TiledTextureRegion mExplodeTextureRegion;
	
	protected MenuScene mMenuScene;

	private Texture mMenuTexture;
	protected TextureRegion mMenuResetTextureRegion;
	protected TextureRegion mMenuQuitTextureRegion;

	private boolean menu = false;
	private ChangeableText titleText;
	private ChangeableText touchText;
	private ChangeableText elapsedText;
	private ChangeableText endText;
	private ChangeableText scoreText;
	private IUpdateHandler timerUpdateHandler;
	private IUpdateHandler bulletUpdateHandler;
	private IUpdateHandler starUpdateHandler;
	private float time = 0.0f;
	
	private AnimatedSprite[] bullet;
	private PhysicsHandler[] bulletHandler;
	
	private Rectangle[] star;
	private PhysicsHandler[] starHandler;
	private PhysicsHandler physicsHandler;
	private boolean gameEnd = false;
	private AnimatedSprite ship;
	private SensorManager sensorManager;
	
	private Music mBackgroundMusic;
	private Sound mExplosionSound;
	
	public Engine onLoadEngine() {	
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		helper = new SQLite(this);
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera).setNeedsMusic(true).setNeedsSound(true));
	}
	
	
	public void onLoadResources() {	
		//Graphic Texture
		TextureRegionFactory.setAssetBasePath("gfx/");
		this.mBulletTexture = new Texture(16, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mBulletTextureRegion = TextureRegionFactory.createTiledFromAsset(this.mBulletTexture, this, "bulletani.png", 0, 8, 2, 1);	
		this.mExplodeTexture = new Texture(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mExplodeTextureRegion = TextureRegionFactory.createTiledFromAsset(this.mExplodeTexture, this, "explode.png", 0, 0, 4, 4);
		this.mEngine.getTextureManager().loadTextures(this.mBulletTexture,this.mExplodeTexture);
		
		//Font Texture
		FontFactory.setAssetBasePath("font/");
		this.mKingdomOfHeartsFontTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mKingdomOfHeartsFont = FontFactory.createFromAsset(this.mKingdomOfHeartsFontTexture, this, "KingdomOfHearts.ttf", 50, true, Color.WHITE);	
		
		//this.mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		//this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC), 30, true, Color.RED);
		
		this.mEngine.getTextureManager().loadTextures(this.mKingdomOfHeartsFontTexture);	
		this.mEngine.getFontManager().loadFonts(this.mKingdomOfHeartsFont);
		
		//Menu Texture
		this.mMenuTexture = new Texture(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mMenuResetTextureRegion = TextureRegionFactory.createFromAsset(this.mMenuTexture, this, "menu_reset.png", 0, 0);
		this.mMenuQuitTextureRegion = TextureRegionFactory.createFromAsset(this.mMenuTexture, this, "menu_quit.png", 0, 50);
		this.mEngine.getTextureManager().loadTexture(this.mMenuTexture);
		
		//Music
		MusicFactory.setAssetBasePath("mfx/");
		try {
			this.mBackgroundMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "rockit.ogg");
			this.mBackgroundMusic.setLooping(true);
		} catch (final IOException e) {
			Debug.e(e);
		}
		SoundFactory.setAssetBasePath("mfx/");
		try {
			this.mExplosionSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "explosion.ogg");
		} catch (final IOException e) {
			Debug.e(e);
		}
	}
	
	
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.createMenuScene();
		
		this.scene = new Scene(1);
		
		this.mBackgroundMusic.play();
		
		this.scene.setBackground(new ColorBackground(0.0f, 0.0f, 0.0f));
		
		titleText = new ChangeableText(300, 170, this.mKingdomOfHeartsFont, "Why So Many Bullets");
		titleText.setScale(2.5f);
		this.scene.getLastChild().attachChild(titleText);
		
		touchText = new ChangeableText(340, 320, this.mKingdomOfHeartsFont, "Touch to Start");

		touchText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.scene.getLastChild().attachChild(touchText);
		
		final Random random = new Random();
		int i;
		star = new Rectangle[STAR_NUMBER];
		starHandler = new PhysicsHandler[STAR_NUMBER];
		for(i = 0; i < STAR_NUMBER; i++)
		{
			star[i] = new Rectangle(854 * random.nextFloat(), 480 * random.nextFloat(), 2, 2);
			starHandler[i] = new PhysicsHandler(star[i]);
			star[i].registerUpdateHandler(starHandler[i]);
			starHandler[i].setVelocity((float)0, (float)(50 * random.nextFloat()));
			this.scene.getLastChild().attachChild(star[i]);
		}
		// END
		this.scene.setOnSceneTouchListener(new IOnSceneTouchListener(){

			public boolean onSceneTouchEvent(Scene pScene,
					TouchEvent pSceneTouchEvent) {
				titleText.setText("");
				touchText.setText("");
				gamestart();
				
				return false;
			}
		});
		starUpdateHandler = new IUpdateHandler() {
			
			public void reset() { 

			}

			public void onUpdate(final float pSecondsElapsed) {
				for(int i = 0;i < STAR_NUMBER; i++)
				{
					if( star[i].getY()>480 )
					{
						star[i].setPosition(854 * random.nextFloat(), 0);
						starHandler[i].setVelocity(0, (float)50 * (random.nextFloat()));
					}
				}
			}
			
		};
		
		this.scene.registerUpdateHandler(starUpdateHandler);
		return this.scene;
	}
	
	
	public void onLoadComplete() {

	}
	
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {			
			if(menu) {
				/* Remove the menu and reset it. */
				menu = !menu;
				this.mMenuScene.back();
			} else {
				/* Attach the menu. */
				menu = !menu;		
				this.scene.setChildScene(this.mMenuScene,false,true,true);				
			}
			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}
	
	
	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
		switch(pMenuItem.getID()) {
			case MENU_RESET:
				
				/* Restart the animation. */
				gameReset();
				this.scene.reset();
				/* Remove the menu and reset it. */
				this.scene.clearChildScene();
				this.mMenuScene.reset();

				time = 0.0f;
				menu = false;
				
				return true;
			case MENU_QUIT:
				/* End Activity. */
				this.finish();
				return true;
			default:
				return false;
		}
	}
	

	// ===========================================================
	// Methods
	// ===========================================================
	protected void createMenuScene() {
		this.mMenuScene = new MenuScene(this.mCamera);

		final SpriteMenuItem resetMenuItem = new SpriteMenuItem(MENU_RESET, this.mMenuResetTextureRegion);
		resetMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.addMenuItem(resetMenuItem);

		final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, this.mMenuQuitTextureRegion);
		quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.addMenuItem(quitMenuItem);

		this.mMenuScene.buildAnimations();

		this.mMenuScene.setBackgroundEnabled(false);

		this.mMenuScene.setOnMenuItemClickListener(this);
	}
	
	protected void gamestart(){
		
		
		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		
		
		// HERE START SETTING BACKGROUND
		
		final int centerX = (CAMERA_WIDTH - SHIP_WIDTH) / 2;
		final int centerY = (CAMERA_HEIGHT - SHIP_HEIGHT) / 2;
		endText = new ChangeableText(30, 100, this.mKingdomOfHeartsFont, "Game Over");
		endText.setText("");
		bullet = new AnimatedSprite[BULLET_NUMBER];
		bulletHandler = new PhysicsHandler[BULLET_NUMBER];
		final Random random = new Random();
		int i;
		for(i = 0; i < BULLET_NUMBER; i++)
		{
			int incoming = random.nextInt(4);			
			
			if(incoming == 0)
			{
				
				bullet[i] = new AnimatedSprite(0, 480 * random.nextFloat(), 8, 8, mBulletTextureRegion);
				bulletHandler[i] = new PhysicsHandler(bullet[i]);
				bullet[i].registerUpdateHandler(bulletHandler[i]);
				bulletHandler[i].setVelocity((float)BULLET_SPEED * random.nextFloat(), (float)(BULLET_SPEED * (random.nextFloat() - 0.5)));
				bullet[i].animate(200);
				this.scene.getLastChild().attachChild(bullet[i]);
			}
			else if(incoming == 1)
			{
				bullet[i] = new AnimatedSprite(854 * random.nextFloat(), 0, 8, 8, mBulletTextureRegion);
				bulletHandler[i] = new PhysicsHandler(bullet[i]);
				bullet[i].registerUpdateHandler(bulletHandler[i]);
				bulletHandler[i].setVelocity((float)(BULLET_SPEED * (random.nextFloat() - 0.5)) , (float)BULLET_SPEED * random.nextFloat());
				bullet[i].animate(200);
				this.scene.getLastChild().attachChild(bullet[i]);
			}
			else if(incoming == 2)
			{
				bullet[i] = new AnimatedSprite(854, 480 * random.nextFloat(), 8, 8, mBulletTextureRegion);
				bulletHandler[i] = new PhysicsHandler(bullet[i]);
				bullet[i].registerUpdateHandler(bulletHandler[i]);
				bulletHandler[i].setVelocity((float)-BULLET_SPEED * random.nextFloat(), (float)(BULLET_SPEED * (random.nextFloat() - 0.5)));
				bullet[i].animate(200);
				this.scene.getLastChild().attachChild(bullet[i]);
			}
			else if(incoming == 3)
			{
				bullet[i] = new AnimatedSprite(854 * random.nextFloat(), 480, 8, 8, mBulletTextureRegion);
				bulletHandler[i] = new PhysicsHandler(bullet[i]);
				bullet[i].registerUpdateHandler(bulletHandler[i]);
				bulletHandler[i].setVelocity((float)(BULLET_SPEED * (random.nextFloat() - 0.5)) , (float)-BULLET_SPEED * random.nextFloat());
				bullet[i].animate(200);
				this.scene.getLastChild().attachChild(bullet[i]);
			}
			
		}
		
		
		ship = new AnimatedSprite(centerX, centerY, this.mExplodeTextureRegion);
		physicsHandler = new PhysicsHandler(ship);
		ship.animate(new long[] {200,200},0,1,true);
		ship.registerUpdateHandler(physicsHandler);
		this.scene.getLastChild().attachChild(ship);
		
		//Timer
		elapsedText = new ChangeableText(30, 30, this.mKingdomOfHeartsFont, "You Survived","You Survived XXXXX s".length());
		scoreText = new ChangeableText(300, 350, this.mKingdomOfHeartsFont, "Highest Survival xxxxx s", "Highest Survival xxxxx s".length());
		scoreText.setText("");
		
		this.scene.getLastChild().attachChild(elapsedText);
		this.scene.getLastChild().attachChild(scoreText);
		/* The actual collision-checking. */
		bulletUpdateHandler = new IUpdateHandler() {
		
			public void reset() { 

			}

			public void onUpdate(final float pSecondsElapsed) {
				//Set Boundary of the ship
				if(ship.getX()<=0)
					ship.setPosition(0, ship.getY());
				if(ship.getX()>=832)
					ship.setPosition(832, ship.getY());
				if(ship.getY()<=0)
					ship.setPosition(ship.getX(), 0);
				if(ship.getY()>=448)
					ship.setPosition(ship.getX(), 448);
				
				for(int i=0;i<BULLET_NUMBER;i++){
					//Check collision
					if(bullet[i].collidesWith(ship)) {			
						if(!gameEnd){
							gameover();						
						}
					} 
					//Regenerate the bullet
					if( bullet[i].getX()<0 || bullet[i].getX()>854 || bullet[i].getY()<0 || bullet[i].getY()>480 )
					{
						int incoming = random.nextInt(4);
						if(incoming == 0)
						{
							bullet[i].setPosition(0, 480 * random.nextFloat());
							bulletHandler[i].setVelocity((float)BULLET_SPEED * random.nextFloat(), (float)(BULLET_SPEED * (random.nextFloat() - 0.5)));					
						}
						else if(incoming == 1)
						{
							bullet[i].setPosition(854 * random.nextFloat(), 0);
							bulletHandler[i].setVelocity((float)(BULLET_SPEED * (random.nextFloat() - 0.5)) , (float)BULLET_SPEED * random.nextFloat());
						}
						else if(incoming == 2)
						{
							bullet[i].setPosition(854, 480 * random.nextFloat());
							bulletHandler[i].setVelocity((float)-BULLET_SPEED * random.nextFloat(), (float)(BULLET_SPEED * (random.nextFloat() - 0.5)));						
						}
						else if(incoming == 3)
						{
							bullet[i].setPosition(854 * random.nextFloat(), 480);
							bulletHandler[i].setVelocity((float)(BULLET_SPEED * (random.nextFloat() - 0.5)) , (float)-BULLET_SPEED * random.nextFloat());					
						}
						
					}
					
				}
				
				//Regenerate the star
				for(int i = 0;i < STAR_NUMBER; i++)
				{
					if( star[i].getY()>480 )
					{
						star[i].setPosition(854 * random.nextFloat(), 0);
						starHandler[i].setVelocity(0, (float)50 * (random.nextFloat()));
					}
				}
			}
			
		};	
		this.scene.registerUpdateHandler(bulletUpdateHandler);
		
		// Time Text
		timerUpdateHandler = new TimerHandler(0.1f, true, new ITimerCallback() {
			public void onTimePassed(final TimerHandler pTimerHandler) {
				time += 0.1f;
				elapsedText.setText("You survived " + String.format("%.1f",time) + " s" );
			}
		});
		
		this.scene.registerUpdateHandler(timerUpdateHandler);
		this.scene.setOnSceneTouchListener(null);

	}
	
	protected void gameover(){
		gameEnd = true;
		int result = (int) (time*100);
		addRow("neo", result); 
		
		
		 
		this.scene.unregisterUpdateHandler(timerUpdateHandler);
		//Explosion Sound
		MainGame.this.mExplosionSound.play();
		//Animation of Explosion
		ship.animate(new long[] {100,100,100,100,100,100,100,100,100,100,100,100,100,100}, 2, 15, 0, new IAnimationListener () {
	           public void onAnimationEnd(final AnimatedSprite pAnimatedSprite) {
	                        runOnUpdateThread(new Runnable() {
	                                public void run() {
	                                	Cursor cursor = getTable();   
	                            		scoreText.setText(showTable(cursor));
	                                	showMenu();	
	                                }
	                        });
	          };
		});
		
		physicsHandler.setVelocity(0);
		ship.unregisterUpdateHandler(physicsHandler);
		endText.setText("Game Over");
		this.scene.getLastChild().attachChild(endText);
		menu = true;	
		
	}
	
	protected void showMenu(){
		 this.scene.setChildScene(this.mMenuScene,false,true,true);		
	}
	
	protected void gameReset(){
		gameEnd = false;
		endText.setText("");
		scoreText.setText("");
		this.scene.registerUpdateHandler(timerUpdateHandler);
		ship.animate(new long[] {200,200},0,1,true);
		ship.registerUpdateHandler(physicsHandler);
		
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================


	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}


	public void onSensorChanged(SensorEvent event) {
		// check sensor type
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
			// assign directions		
			float x=event.values[0];
			float y=event.values[1];			
			physicsHandler.setVelocity(y * 70, x * 70);
		}
		
	}
	
	private void addRow(String name, int value){     // 加入一個記錄            
        SQLiteDatabase db = helper.getWritableDatabase();  
        ContentValues values = new ContentValues();  
        values.put("name", name);  
        values.put("value", value);  
        db.insertOrThrow("score", null, values);  
   }
	
	private Cursor getTable() {  
        SQLiteDatabase db = helper.getReadableDatabase();  
        Cursor cursor = db.query("score", FROM, null, null, null, null, ORDER_BY);  
        startManagingCursor(cursor);  
        return cursor;  
   }  
	
	 private String showTable(Cursor cursor) {  
		 cursor.moveToNext();
		 float score = (float)(cursor.getInt(2))/100;
		 String score_s = "Highest Survival "+ String.format("%.1f", score) +" s";
		 cursor.moveToFirst();
         cursor.close();
         return score_s;
    }  
}
