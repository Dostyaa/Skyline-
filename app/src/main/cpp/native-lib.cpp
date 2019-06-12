#include <jni.h>
#include <string>
#include <sstream>
#include <cmath>
#include <cstdio>
#include <iostream>
#include <fstream>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>


#define PI 3.14159265358979323846
#include "toto.h"

extern "C" {


double AzimutImage; //Direction en degres de la prise de vue                            !!! 0∞ Ètant l'azimut plein EST !!!
double ChampDeVueCapteur; //Champ de vue en degres du telephone                         !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
int ResolutionAngulaire; //Modifie la dÈformation horizontal

float* HMaxCrete[3];
int* POSCrete[2];

double Carte3D[3003][3003];
double CarteCrete[3003][3003];
//CImageDouble CarteCretePol;
std::string NomCarteOrigine; //Carte contenant ma postion
int Xrelatif;
int Yrelatif;
double MyZPosition = 0;
int DistanceMin = 400; //Distance en mËtres entre ma position et le premier point 3D lu.


void LireTable(std::string NomCarte, int OffSetX, int OffSetY, AAssetManager *mgr) {

    // Ouverture du dossier contenant les fichiers BDD
    std::string nom = "BDALTI_25M_" + NomCarte + "_COMP.txt";
    const char* affich = nom.c_str();
    __android_log_print(ANDROID_LOG_DEBUG,"tryy","%s",affich);
    // Utilisation librairie AssetManager pour ouvrir le flux vers apk
    AAsset * asset = AAssetManager_open(mgr,nom.c_str(),3);
    // Extraction du buffer
    const char* buff = (char*)AAsset_getBuffer(asset);
    // Passage vers un fichier string pour utiliser le stringstream de gregou
    std::string tmp(buff,(unsigned long)AAsset_getLength(asset));
    std::stringstream buffer(tmp,std::ios::in);

    std::string contenu;

    int X = 0;
    int Y = 0;


    if (buffer)
    {
        int Z = 0;
        X = 0;
        Y = 0;
        if (NomCarte == NomCarteOrigine)
        {
            while (buffer)
            {
                if (X == 0)
                {
                    buffer >> Z;
                }
                else
                {
                    double ZPlus = 0 ;
                    buffer >> ZPlus;
                    Z += ZPlus;
                }

                Carte3D[3000 - (2000 + (1000 * OffSetY) - Y)][(1000 + (1000 * OffSetX) + X)] = Z;//permet de ne prendre que les 8 bits de l'entier qui


                if (X == Xrelatif && Y == 1000 - Yrelatif)
                {
                    if (Z == -99999)
                        MyZPosition = 0;
                    else
                        MyZPosition = Z; //Je rÈcupËre mon altitude
                }

                X++;
                if (X == 1000)
                {
                    Y++;
                    X = 0;
                }
            }
            // std::cout << "J'ai fini !" << std::endl;
        }
        else
        {
            while (buffer)
            {
                if (X == 0)
                {
                    buffer >> Z;

                }
                else
                {
                    double ZPlus = 0 ;
                    buffer >> ZPlus;
                    Z += ZPlus;
                }
                Carte3D[3000 - (2000 + (1000 * OffSetY) - Y)][(1000 + (1000 * OffSetX) + X)] = Z;//permet de ne prendre que les 8 bits de l'entier qui

                X++;
                if (X == 1000)
                {
                    Y++;
                    X = 0;
                }
            }

        }

    }
    else  // sinon
        std::cerr << "Impossible d'ouvrir le fichier !" << std::endl;
    AAsset_close(asset);
    return;
}
float CubicHermite(float A, float B, float C, float D, float t)
{
    float a = -A / 2.0f + (3.0f*B) / 2.0f - (3.0f*C) / 2.0f + D / 2.0f;
    float b = A - (5.0f*B) / 2.0f + 2.0f*C - D / 2.0f;
    float c = -A / 2.0f + C / 2.0f;
    float d = B;

    return a*t*t*t + b*t*t + c*t + d;
}


void RemplissageHorizon(int Xrelatif, int Yrelatif, int iDeb, int iFin, int jDeb, int jFin) {

    float DistanceAbsX;
    float DistanceAbsY;
    float ValCarteCrete;
    float DistanceVol;
    float PixelTetaRotZ = 0;
    bool ValMet = false;


    //Gestion des bords car utilisation de matrice 4*4
    if (iDeb == 0)
        iDeb = 1;
    if (jDeb == 0)
        jDeb = 1;
    if (iFin == 3000)
        iFin = 2998;
    if (jFin == 3000)
        jFin = 2998;

    bool Crete = false;

    for (int i = iDeb; i < iFin; i++)
    {
        for (int j = jDeb; j < jFin; j++)
        {
            ValMet = false;
            Crete = false;

            DistanceAbsX = ((float)j - (float)Xrelatif) * 25; // * 25 -> Passage 25m entre chaque points
            DistanceAbsY = ((float)Yrelatif - (float)i) * 25; // * 25 -> Passage 25m entre chaque points
            DistanceVol = sqrt((DistanceAbsX * DistanceAbsX) + (DistanceAbsY * DistanceAbsY)); //Distance à Vol d'oiseau du point observé depuis ma postion en mètre

            if (DistanceVol > 800)
            {
                PixelTetaRotZ = (atan2((DistanceAbsY), (DistanceAbsX)) * (180 / PI)); //Angle en degres


                if (PixelTetaRotZ <= 0)
                    PixelTetaRotZ = 360 + PixelTetaRotZ;

                ValCarteCrete = (Carte3D[i][j] - MyZPosition) / DistanceVol;
                float Angle = (atan(ValCarteCrete) * 180) / PI;


                if (HMaxCrete[0][static_cast<int>(((390)*ResolutionAngulaire) - ((PixelTetaRotZ)* ResolutionAngulaire))] < Angle)
                {
                    HMaxCrete[0][static_cast<int>(((390)*ResolutionAngulaire) - ((PixelTetaRotZ)* ResolutionAngulaire))] = Angle;
                    HMaxCrete[1][static_cast<int>(((390)*ResolutionAngulaire) - ((PixelTetaRotZ)* ResolutionAngulaire))] = DistanceVol;

                    POSCrete[0][static_cast<int>(((390)*ResolutionAngulaire) - ((PixelTetaRotZ)* ResolutionAngulaire))] = i;
                    POSCrete[1][static_cast<int>(((390)*ResolutionAngulaire) - ((PixelTetaRotZ)* ResolutionAngulaire))] = j;
                }
            }
        }
    }

    return;
}

void RemplissageInterpolation(int Xrelatif, int Yrelatif, int i, int j)
{
    float NbValInterpol = 120;
    float ValCoefInterpol = 1 / NbValInterpol;
    float NewZ[int(NbValInterpol)][int(NbValInterpol)];
    float PixelTetaRotZ = 0;

    int ImpResX = (((double)abs(Xrelatif - j) / (double)160) * 1) + 1;
    int ImpResY = (((double)abs(Yrelatif - i) / (double)160) * 1) + 1;

    for (int m = 0; m < NbValInterpol; m += ImpResY)
    {
        for (int n = 0; n < NbValInterpol; n += ImpResX)
        {
            float DistanceAbsX = (((float)j + ((float)n / NbValInterpol)) - (float)Xrelatif) * 25; // * 25 -> Passage 25m entre chaque points
            float DistanceAbsY = ((float)Yrelatif - ((float)i + ((float)m / NbValInterpol))) * 25; // * 25 -> Passage 25m entre chaque points
            float DistanceVol = sqrt((DistanceAbsX * DistanceAbsX) + (DistanceAbsY * DistanceAbsY)); //Distance à Vol d'oiseau du point observé depuis ma postion en mètre


            float Temp = atan2((DistanceAbsY), (DistanceAbsX)); //Angle en radian


            PixelTetaRotZ = (Temp * (180 / PI)); // Normalise sur 360 degrès l'angle entre le plein Est et le point observé

            if (PixelTetaRotZ <= 0)
                PixelTetaRotZ = 360 + PixelTetaRotZ;


            if (m == 0 && n == 0)
            {
                // 1st row
                float p00 = Carte3D[i - 1][j - 1];
                float p10 = Carte3D[i - 1][j + 0];
                float p20 = Carte3D[i - 1][j + 1];
                float p30 = Carte3D[i - 1][j + 2];

                // 2nd row
                float p01 = Carte3D[i + 0][j - 1];
                float p11 = Carte3D[i + 0][j + 0];
                float p21 = Carte3D[i + 0][j + 1];
                float p31 = Carte3D[i + 0][j + 2];

                // 3rd row
                float p02 = Carte3D[i + 1][j - 1];
                float p12 = Carte3D[i + 1][j + 0];
                float p22 = Carte3D[i + 1][j + 1];
                float p32 = Carte3D[i + 1][j + 2];

                // 4th row
                float p03 = Carte3D[i + 2][j - 1];
                float p13 = Carte3D[i + 2][j + 0];
                float p23 = Carte3D[i + 2][j + 1];
                float p33 = Carte3D[i + 2][j + 2];



                for (int fy = 0; fy < NbValInterpol; fy++)
                {
                    for (int gx = 0; gx < NbValInterpol; gx++)
                    {
                        float col0 = CubicHermite(p00, p10, p20, p30, ValCoefInterpol*gx);
                        float col1 = CubicHermite(p01, p11, p21, p31, ValCoefInterpol*gx);
                        float col2 = CubicHermite(p02, p12, p22, p32, ValCoefInterpol*gx);
                        float col3 = CubicHermite(p03, p13, p23, p33, ValCoefInterpol*gx);
                        NewZ[fy][gx] = CubicHermite(col0, col1, col2, col3, ValCoefInterpol*fy);
                    }
                }
            }

            float ValCarteCrete = (NewZ[m][n] - MyZPosition) / DistanceVol;
            float Angle = (atan(ValCarteCrete) * 180) / PI;

            if (HMaxCrete[0][static_cast<int>(((390)*ResolutionAngulaire) - ((PixelTetaRotZ)* ResolutionAngulaire))] < Angle)
            {
                HMaxCrete[0][static_cast<int>(((390)*ResolutionAngulaire) - ((PixelTetaRotZ)* ResolutionAngulaire))] = Angle;
                HMaxCrete[1][static_cast<int>(((390)*ResolutionAngulaire) - ((PixelTetaRotZ)* ResolutionAngulaire))] = DistanceVol;

                POSCrete[0][static_cast<int>(((390)*ResolutionAngulaire) - ((PixelTetaRotZ)* ResolutionAngulaire))] = -1;
                POSCrete[1][static_cast<int>(((390)*ResolutionAngulaire) - ((PixelTetaRotZ)* ResolutionAngulaire))] = -1;

            }
        }
    }
    return;
}


float** skyline_Virtuel(double latitude, double longitude, int ResolAngle, AAssetManager *mgr)
{
    //                                                                                     !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    AzimutImage = 180; //Direction en degres de la prise de vue                            !!! 0∞ Ètant l'azimut plein EST !!!
    ChampDeVueCapteur = 360; //Champ de vue en degres du telephone                         !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    ResolutionAngulaire = ResolAngle; //Modifie la dÈformation horizontal


    HMaxCrete[0] = new float[(360+60)*ResolutionAngulaire];
    HMaxCrete[1] = new float[(360+60)*ResolutionAngulaire];
    HMaxCrete[2] = new float[1];

    POSCrete[0] = new int[(360 + 60)*ResolutionAngulaire];
    POSCrete[1] = new int[(360 + 60)*ResolutionAngulaire];

    for (int i = 0; i < ((360 + 60) * 30); i++)
    {
        HMaxCrete[0][i] = -50;
        HMaxCrete[1][i] = -50;
        POSCrete[0][i] = -1;
        POSCrete[1][i] = -1;
    }

    ////////////////////////////////////////////
    //      dÈfinition des constantes et      //
    // rÈcupÈration des coordonnÈes en WGS-84 //
    ////////////////////////////////////////////
    int MyXPosition, MyYPosition;

    int OpenTabX = 0, OpenTabY = 0;

    float c = 11754255.426096; //constante de la projection
    float e = 0.0818191910428158; //premiËre exentricitÈ de l'ellipsoÔde
    float n = 0.725607765053267; //exposant de la projection
    float xs = 700000; //coordonnÈes en projection du pole
    float ys = 12655612.049876; //coordonnÈes en projection du pole

    // prÈ-calculs
    float lat_rad = latitude / 180 * PI; //latitude en rad
    float lat_iso = atanh(sin(lat_rad)) - e*atanh(e*sin(lat_rad)); //latitude isomÈtrique

    //calcul
    float x = ((c*exp(-n*(lat_iso)))*sin(n*(longitude - 3) / 180 * PI) + xs);
    float y = (ys - (c*exp(-n*(lat_iso)))*cos(n*(longitude - 3) / 180 * PI));

    printf("x : %lf\ny : %lf\n", x, y);
    printf("./");

    MyXPosition = x;
    MyXPosition /= 25; //Arrondi au pas de 25 le plus proche

    MyYPosition = y;
    MyYPosition /= 25; // 259 477


    OpenTabX = (x / 25000); //Point origine de la carte en X coordonnÈes planes
    OpenTabX *= 25; //Conversion en Entier avant multiplication
    OpenTabY = (y / 25000); //Point origine de la carte en Y coordonnÈes planes
    OpenTabY *= 25; //Conversion en Entier avant multiplication
    OpenTabY += 25;

    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------//


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //DÈfinition du nombre de base connexes V8 ‡ analyser /// 1 = CarrÈ de 3*3 bases // 2 = CarrÈ de 5*5 bases // .... ///////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    int BaseVoisine = 1;
    int DebutOuvBaseX, FinOuvBaseX;
    int DebutOuvBaseY, FinOuvBaseY;

    double tempDebX = 0, tempFinX = 0;

    tempDebX = cos((AzimutImage + (ChampDeVueCapteur / 2))* 3.1415926535897932384626433832795 / 180);
    tempFinX = cos((AzimutImage - (ChampDeVueCapteur / 2))* 3.1415926535897932384626433832795 / 180);

    if (tempDebX > tempFinX)
    {
        DebutOuvBaseX = tempFinX - 0.5;
        FinOuvBaseX = tempDebX + 0.5;
    }
    else
    {
        DebutOuvBaseX = tempDebX - 0.5;
        FinOuvBaseX = tempFinX + 0.5;
    }

    double tempDebY = 0, tempFinY = 0;

    tempDebY = sin((AzimutImage)* 3.1415926535897932384626433832795 / 180);
    tempFinY = sin((AzimutImage + (ChampDeVueCapteur / 2))* 3.1415926535897932384626433832795 / 180);

    if (tempDebY > tempFinY)
    {
        DebutOuvBaseY = tempFinY - 0.5;
        FinOuvBaseY = tempDebY + 0.5;
    }
    else
    {
        DebutOuvBaseY = tempDebY - 0.5;
        FinOuvBaseY = tempFinY + 0.5;
    }




    DebutOuvBaseX = -1;
    DebutOuvBaseY = -1;
    FinOuvBaseX = 1;
    FinOuvBaseY = 1;
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------//


    //////////////////////////////////////////////////
    ///// Recherche de l'altitude de ma position /////
    /////           dans la base donnÈes         /////
    //////////////////////////////////////////////////

    std::string ligne;
    std::ostringstream CarteAOuvrirOrigine;
    if (OpenTabX < 1000)
        CarteAOuvrirOrigine << "0" << OpenTabX << "_" << OpenTabY;
    else
        CarteAOuvrirOrigine << OpenTabX << "_" << OpenTabY;
    NomCarteOrigine += CarteAOuvrirOrigine.str();


    int PosTabOrigineX = OpenTabX, PosTabOrigineY = OpenTabY;

    //system("pause");
    OpenTabX *= 1000;
    OpenTabX /= 25;

    OpenTabY *= 1000;
    OpenTabY /= 25;

    Xrelatif = MyXPosition - OpenTabX;
    Yrelatif = (1000 - (OpenTabY - MyYPosition));

    //Lecture du Z faite pendant la lecture des tables.


    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------//

    ////////////////////////////////////
    //DÈbut de l'analyse des tables 3D//
    ////////////////////////////////////




    for (int StartXCarte = -1; StartXCarte <= 1; StartXCarte++)
        //for (int StartXCarte = 0; StartXCarte < 1; StartXCarte++)
    {
        for (int StartYCarte = -1; StartYCarte < 2; StartYCarte++)
            //for (int StartYCarte = 0; StartYCarte < 1; StartYCarte++)
        {
            std::string NomCarte;
            std::ostringstream CarteAOuvrir;

            if ((PosTabOrigineX + (25 * StartXCarte)) < 1000)
                CarteAOuvrir << "0" << PosTabOrigineX + (25 * StartXCarte) << "_" << PosTabOrigineY + (25 * StartYCarte);
            else
                CarteAOuvrir << PosTabOrigineX + (25 * StartXCarte) << "_" << (PosTabOrigineY + (25 * StartYCarte));


            NomCarte += CarteAOuvrir.str();

            LireTable(NomCarte, StartXCarte, StartYCarte, mgr);


        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------//


    //////////////////////////////////////////////
    //DÈbut de du remplissage de l'image polaire//
    //////////////////////////////////////////////

    Xrelatif = (Xrelatif + 1000 * BaseVoisine);
    Yrelatif = (1000 - Yrelatif + (1000 * BaseVoisine));



    for (int StartXCarte = 0; StartXCarte < 3; StartXCarte++)
    {
        for (int StartYCarte = 0; StartYCarte < 3; StartYCarte++)
        {

            RemplissageHorizon(Xrelatif, Yrelatif, 1000 * StartXCarte, 1000 * (StartXCarte+1), 1000 * StartYCarte, 1000 * (StartYCarte+1));
        }
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------//


    ///////////////////////////////////////////////
    //DÃˆbut de du remplissage de l'image polaire//
    ///////////////////////////////////////////////


    //Thread pour le remplissage de l'image polaire -> 1 thread pour chaque base.
    int compteur = 0;
    for (int k = (30 * ResolutionAngulaire); k < (390)*ResolutionAngulaire; k++)
    {
        if (POSCrete[0][k] != -1 && POSCrete[1][k] != -1)
        {
            RemplissageInterpolation(Xrelatif, Yrelatif, POSCrete[0][k], POSCrete[1][k]);
            compteur++;
        }
    }

    //std::cout << "Nombre de valeur interpolé :" << compteur << std::endl;

//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------//

    for (int i = 0; i <= (30 * ResolutionAngulaire); i++)
    {
        HMaxCrete[0][i] = HMaxCrete[0][(360 * int(ResolutionAngulaire)) + i];
        HMaxCrete[1][i] = HMaxCrete[1][(360 * int(ResolutionAngulaire)) + i];
        HMaxCrete[0][(390 * int(ResolutionAngulaire)) + i] = HMaxCrete[0][(30 * int(ResolutionAngulaire)) + i];
        HMaxCrete[1][(390 * int(ResolutionAngulaire)) + i] = HMaxCrete[1][(30 * int(ResolutionAngulaire)) + i];
    }

    HMaxCrete[2][0] = float(MyZPosition);

    //std::cout << "Angle au plein Est : " << HMaxCrete[0][(420 * int(ResolutionAngulaire))] << std::endl;

    return HMaxCrete;
}

JNIEXPORT jfloatArray JNICALL Java_com_example_dostya_cpptojava_bddextract_mainjni(JNIEnv*env, jobject instance, jobject assetMngr, jdouble latitude, jdouble longitude, jint ResolAng)
{
    jfloatArray out=env->NewFloatArray(((360+60)*ResolAng*2+1));
    // Librairie AssetManager pour lecture des fichiers dans l'apk
     AAssetManager *mgr = AAssetManager_fromJava(env,assetMngr);

    // Lancement algorithme - recu 5/5 general gregouzzzzz
    float** extract = skyline_Virtuel(latitude,longitude,ResolAng,mgr);

    // Concat results dans le tableau de float
    env->SetFloatArrayRegion(out,0,(360+60)*ResolAng -1,extract[0]);
    env->SetFloatArrayRegion(out,(360+60)*ResolAng,((360+60)*ResolAng-1),extract[1]);
    env->SetFloatArrayRegion(out,((360+60)*ResolAng*2),1,extract[2]);

    //free(extract);

    return(out);
}


}





