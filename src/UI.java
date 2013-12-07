import javax.microedition.lcdui.Graphics;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;


/* --------------------------
 * Projet NXT Timelapse
 * Ecrit par Merlin Nimier-David
 * License Creative Commons
 *
 * Cette classe gère l'interface utilisateur : l'écran titre, la sélection des paramètres, ...
 *
 * CONFIGURATION DES MOTEURS:
 * A: déclancheur
 * B: roues
 * C: roue de sélection
 */

public class UI {

    private String[] modes;
    private int modeChoisi;
    private String[] features;
    private int featureChoisie;
    private int dureeChoisie; //En min
    private int intervalChoisi; //En s

    private int distanceChoisie;
    private int angleChoisi;


    //Pour la progress bar
    Graphics canvas;
    double avancement;
    int largeurBarre;
    String pourcentage;


    public UI()
    { //CONSTRUCTEUR
	modes = new String[2];
	    modes[0] = "Photo";
	    modes[1] = "Video";

	features = new String[4];
	    features[0] = "Still";
	    features[1] = "Pan";
	    features[2] = "Rotate";
	    features[3] = "Pan+Rotate";

	//Valeurs par défaut
	modeChoisi = 0;
	featureChoisie = 0;
	dureeChoisie = 5;
	intervalChoisi = 5;

	distanceChoisie = 0;
	angleChoisi = 0;

	//Pour la progress bar
	canvas = new Graphics();
	avancement = 0;
	largeurBarre = 0;
	pourcentage = "";

	//Pour la roue de sélection
	//Motor.C.smoothAcceleration(false);
	//Motor.C.regulateSpeed(false);
	Motor.C.setBrakePower(0); //On s'assure que la roue de sélection n'est pas bloquée
    }

    public void ecranTitre()
    { //ecranTitre : affiche le nom du programme, l'auteur, etc
	System.out.println("** Timelapse ***");
	System.out.println("**** Merlin ****");
	System.out.println("* Nimier-David *");
	System.out.println(" ");
	System.out.println(" ");
	Button.ENTER.waitForPress();
    }

    //Setup wizzard
    public void setup()
    { //setup : choix des paramètres par l'utilisateur
	LCD.clear();

	//Choix du mode
	selectMode();
	//Choix de la feature
	while(Button.readButtons() != 0) { /*On ne fait rien tant que le bouton n'a pas été relaché*/ }
	selectFeature();
	if(modeChoisi != 1) //Seulement si le mode n'est pas video
	{
	    //Choix de l'interval
	    while(Button.readButtons() != 0) { /*On ne fait rien tant que le bouton n'a pas été relaché*/ }
	    selectInterval();
	}
	else
	    intervalChoisi = 5;
	//Choix de la durée totale
	while(Button.readButtons() != 0) { /*On ne fait rien tant que le bouton n'a pas été relaché*/ }
	selectDuree();
	
	//Paramètres optionnels
	if(featureChoisie == 1 || featureChoisie == 3)
	{
	    distanceChoisie = 15; //Valeur par défaut
	    while(Button.readButtons() != 0) { /*On ne fait rien tant que le bouton n'a pas été relaché*/ }
	    selectDistance();
	}
	if(featureChoisie == 2 || featureChoisie == 3)
	{
	    angleChoisi = 60; //Valeur par défaut
	    while(Button.readButtons() != 0) { /*On ne fait rien tant que le bouton n'a pas été relaché*/ }
	    selectAngle();
	}
	    
    }

    //Résumé
    public void sumUp()
    {
	LCD.clear();
	if(modeChoisi == 0) //Mode photo
	    System.out.println("I'm gonna take a picture every " + intervalChoisi + "s for " + dureeChoisie + "min.");
	else
	    System.out.println("I'm gonna shoot video for " + dureeChoisie + "min.");
	
	if(featureChoisie == 1) //Si on fait un panning
	{
	    System.out.println("After " + dureeChoisie + "min, I will have travelled " + distanceChoisie + "cm");
	}
	
	System.out.println(" ");
	System.out.println("Is that okay ?");
	
	//Entrer ou annuler ?
	int input = Button.waitForPress();
	//Tant que l'on appuye par sur Entrée, on refait le setup
	if(input == Button.ID_ESCAPE)
	{
	    setup();
	    sumUp();
	}
    }

    //Press to start
    public void pressToStart()
    {
	LCD.clear();
	System.out.println("Looks like");
	System.out.println("we're good");
	System.out.println("to go !");
	System.out.println(" ");
	System.out.println("Press to start");
	Button.ENTER.waitForPress();
    }

    //Affichage de la barre de prograssion
    public void refreshProgressBar(int elapsed, double dureeTotale)
    {
	avancement = (elapsed/(dureeTotale*1000));
	largeurBarre = (int)(avancement * 80); //Largeur maximum : 80px
	pourcentage = (int)(avancement*100) + "%";

	canvas.fillRect(15, 10, largeurBarre, 10); //Affichage de la barre
	canvas.drawString(pourcentage, 35, 49);
    }

    //SELECTS
    private void selectMode()
    {
	int input = 0;
	int previousInput = input;

	LCD.clear();
	LCD.drawString("Mode", 36, 5, true);
	LCD.drawString(modes[modeChoisi], 35, 30, false);
	while(input != 9999 && input != -9999)
	{
	    
	    if(input != 0)
	    { //Rien à changer si l'input est nul
		if((modeChoisi + input) > (modes.length - 1))
		{ //Si on dépasse la taille de l'array avec notre input
		    //On va au début ou à la fin de notre array
		    modeChoisi = 0;
		}
		else if((modeChoisi + input) < 0)
		{//Si on dépasse la taille de l'array avec notre input
		    //On va au début ou à la fin de notre array
		    modeChoisi = modes.length - 1;
		}
		else //Si tout se passe bien
		    modeChoisi += input;

		LCD.drawString("          ", 35, 30, false); //Pour éviter le ghosting
		LCD.drawString(modes[modeChoisi], 35, 30, false);
	    }
	    previousInput = input;
	    while(previousInput == input && input != 0) //Pour empecher le ghosting
		input = getInput();
	    input = getInput();
	}
    }
    private void selectFeature()
    {
	int input = 0;
	int previousInput = input;

	LCD.clear();
	LCD.drawString("Feature", 30, 5, true);
	LCD.drawString(features[featureChoisie], 35, 30, false);
	while(input != 9999 && input != -9999)
	{
	    if(input != 0)
	    { //Rien à changer si l'input est nul
		if((featureChoisie + input) > (features.length - 1))
		{ //Si on dépasse la taille de l'array avec notre input
		    //On va au début ou à la fin de notre array
		    featureChoisie = 0;
		}
		else if((featureChoisie + input) < 0)
		{//Si on dépasse la taille de l'array avec notre input
		    //On va au début ou à la fin de notre array
		    featureChoisie = features.length - 1;
		}
		else //Si tout se passe bien
		    featureChoisie += input;

		LCD.drawString("          ", 35, 30, false); //Pour éviter le ghosting
		LCD.drawString(features[featureChoisie], 35, 30, false);
	    }
	    previousInput = input;
	    while(previousInput == input && input != 0) //Pour empecher le ghosting
		input = getInput();
	    input = getInput();
	}
    }
    private void selectInterval()
    {
	int input = 0;
	int previousInput = input;

	LCD.clear();
	LCD.drawString("Interval (s)", 10, 5, true);
	LCD.drawString(String.valueOf(intervalChoisi), 45, 30, false);
	while(input != 9999 && input != -9999)
	{
	    if(input != 0)
	    { //Rien à changer si l'input est nul
		
		if((intervalChoisi + input) < 0)
		{//Raccourci : si on descend sous 0, on passe direct à 1 minutes
		    intervalChoisi = 60;
		}
		else //Si tout se passe bien
		    intervalChoisi += input;

		LCD.drawString("          ", 35, 30, false); //Pour éviter le ghosting
		LCD.drawString(String.valueOf(intervalChoisi), 45, 30, false);
	    }
	    previousInput = input;
	    while(previousInput == input && input != 0) //Pour empecher le ghosting
		input = getInput();
	    input = getInput();
	}
    }
    private void selectDuree()
    {
	int input = 0;
	int previousInput = input;

	LCD.clear();
	LCD.drawString("Duree (min)", 15, 5, true);
	LCD.drawString(String.valueOf(dureeChoisie), 45, 30, false);
	while(input != 9999 && input != -9999)
	{
	    if(input != 0)
	    { //Rien à changer si l'input est nul
		
		if((dureeChoisie + input) < 0)
		{//Raccourci : si on descend sous 0, on passe direct à 30 minutes
		    dureeChoisie = 30;
		}
		else //Si tout se passe bien
		    dureeChoisie += input;


		LCD.drawString("          ", 45, 30, false); //Pour éviter le ghosting
		LCD.drawString(String.valueOf(dureeChoisie), 45, 30, false);
	    }
	    previousInput = input;
	    while(previousInput == input && input != 0) //Pour empecher le ghosting
		input = getInput();
	    input = getInput();
	}
    }
    private void selectDistance()
    {
	int input = 0;
	int previousInput = input;

	LCD.clear();
	LCD.drawString("Distance (cm)", 10, 5, true);
	LCD.drawString(String.valueOf(distanceChoisie), 45, 30, false);
	while(input != 9999 && input != -9999)
	{
	    if(input != 0)
	    { //Rien à changer si l'input est nul
		
		if((distanceChoisie + input) < 0)
		{//Raccourci : si on descend sous 0, on passe direct à 30 minutes
		    distanceChoisie = 30;
		}
		else //Si tout se passe bien
		    distanceChoisie += input;


		LCD.drawString("          ", 45, 30, false); //Pour éviter le ghosting
		LCD.drawString(String.valueOf(distanceChoisie), 45, 30, false);
	    }
	    previousInput = input;
	    while(previousInput == input && input != 0) //Pour empecher le ghosting
		input = getInput();
	    input = getInput();
	}
    }
    private void selectAngle()
    {
	int input = 0;
	int previousInput = input;

	LCD.clear();
	LCD.drawString("Angle (degres)", 1, 5, true);
	LCD.drawString(String.valueOf(angleChoisi), 45, 30, false);
	while(input != 9999 && input != -9999)
	{
	    if(input != 0)
	    { //Rien à changer si l'input est nul
		
		if((angleChoisi + input) < 0)
		{//Raccourci : si on descend sous 0, on passe direct à 30 minutes
		    angleChoisi = 30;
		}
		else //Si tout se passe bien
		    angleChoisi += input;


		LCD.drawString("          ", 45, 30, false); //Pour éviter le ghosting
		LCD.drawString(String.valueOf(angleChoisi), 45, 30, false);
	    }
	    previousInput = input;
	    while(previousInput == input && input != 0) //Pour empecher le ghosting
		input = getInput();
	    input = getInput();
	}
    }

    private int getInput()
    { //Get input : renvoye -1 pour la gauche, 1 pour la droite, 9999 pour Entrée et -9999 pour Annuler
	int valeur = 0;
	int boutonAppuye = Button.readButtons();

	switch(boutonAppuye)
	{
	    case Button.ID_RIGHT:
		valeur = 1;
	    break;
	    case Button.ID_LEFT:
		valeur = -1;
	    break;
	    case Button.ID_ENTER:
		valeur = 9999;
	    break;
	    case Button.ID_ESCAPE:
		valeur = -9999;
	    break;

	    default: //Input par roue de sélection
		Motor.C.resetTachoCount(); //On reset la valeur de rotation

		int i = 0;
		while(i < 10000)
		{ //On coince dans une boucle quelques instants, pour laisser le temps de bouger la roue
		    i++;
		}

		int tacho = Math.abs(Motor.C.getTachoCount());
		if(tacho > 3 && tacho <= 10) //Petit mouvement
		    valeur = -(int)(Motor.C.getTachoCount()%2); //Modulo 2, ainsi on a forcément +1 ou -1
		else if(tacho > 10 && tacho <= 20) //Moyen mouvement
		    valeur = -(int)(Motor.C.getTachoCount()%3); //Modulo 3, ainsi on a forcément des chiffres plus grands
		else if(tacho > 20) //Grand mouvement
		    valeur = -(int)(Motor.C.getTachoCount()%9); //Modulo 6, ainsi on a forcément des chiffres plus grands
		else
		    valeur = 0; //Mouvement trop petit / pas de mouvement
		//if(valeur != 0)
		    //System.out.println(valeur);
	    break;
	}

	return valeur;
    }



    //GETTERS
    public int getMode()
    {
	return modeChoisi + 1; //+1 pour compenser la numérotation à partir de 0 dans les arrays
    }
    public int getFeature()
    {
	return featureChoisie + 1; //+1 pour compenser la numérotation à partir de 0 dans les arrays
    }
    public int getDuree()
    {
	return dureeChoisie * 60; //Conversion en s
    }
    public int getInterval()
    {
	return intervalChoisi;
    }
    public int getDistance()
    {
	return distanceChoisie;
    }
    public int getAngle()
    {
	return angleChoisi;
    }
}
