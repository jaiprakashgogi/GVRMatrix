/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.simplesample;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCubeSceneObject;

import java.io.IOException;
import java.util.concurrent.Future;

public class SampleMain extends GVRScript {

    private GVRContext mGVRContext;
    private Context context;
    private GLSurfaceView glView;
    private Texample2Renderer myRenderer;
    Rain drop;
	private PianoKeys pianoKeys;
    private static final float CUBE_WIDTH = 20.0f;
    private static final float SCALE_FACTOR = 2.0f;

    @Override
    public void onInit(GVRContext gvrContext) {

        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mGVRContext = gvrContext;
        context = gvrContext.getContext();
        GVRScene scene = gvrContext.getNextMainScene();
        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera()
                .setBackgroundColor(Color.GRAY);
        mainCameraRig.getRightCamera()
                .setBackgroundColor(Color.GRAY);
        scene.setStatsEnabled(true);
        scene.setFrustumCulling(true);
        
     
        // Uncompressed cubemap texture
        Future<GVRTexture> futureCubemapTexture = gvrContext.loadFutureCubemapTexture(new GVRAndroidResource(mGVRContext, R.raw.beach));
        GVRMaterial cubemapMaterial = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Cubemap.ID);
        cubemapMaterial.setMainTexture(futureCubemapTexture);


        // List of textures (one per face)
        /*ArrayList<Future<GVRTexture>> futureTextureList = new ArrayList<Future<GVRTexture>>(6);
        futureTextureList.add(gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.back)));
        futureTextureList.add(gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.right)));
        futureTextureList.add(gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.front)));
        futureTextureList.add(gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.left)));
        futureTextureList.add(gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.top)));
        futureTextureList.add(gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.bottom)));
*/

            GVRCubeSceneObject mCubeEvironment = new GVRCubeSceneObject(gvrContext, false, cubemapMaterial);
            mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH, CUBE_WIDTH);
            //uncomment the following line to make cubemap a scene object
            scene.addSceneObject(mCubeEvironment);

        try {
            GVRMesh mesh = gvrContext.loadMesh(new GVRAndroidResource(mGVRContext.getContext(), "shield.obj"));
            GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext.getContext(), "shield.jpg"));
            GVRSceneObject sceneObject = new GVRSceneObject(gvrContext, mesh,
                    texture);
            sceneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
            sceneObject.getTransform().setScale(0.5f, 0.5f, 0.5f);
            // add the scene object to the scene graph
            scene.addSceneObject(sceneObject);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load texture
        /*GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.gearvr_logo));
        GVRSceneObject sceneObject = new GVRSceneObject(gvrContext, 4.0f, 2.0f,
                texture);

        // set the scene object position
        sceneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);

        // add the scene object to the scene graph
        scene.addSceneObject(sceneObject);*/


        //Rain rain = new Rain(gvrContext);
        char c = 0;
        drop = new Rain(gvrContext, -5.f);
        Rain rain = new Rain(gvrContext, -10.f);
        // gvrContext.registerOnDrawFrameListener(new myDrawFrameListener());
        
        //Piano class object
        pianoKeys = new PianoKeys(mGVRContext.getContext());
    }

    /*
    class myDrawFrameListener() extends GVRDrawFrameListener {
        GVRTexture mTexture;
        GVRRenderTexture mRenderTexture;

        @Override
            public void onDrawFrame() {
                // save off the eyebuffer id
                // glGet(GL_FRAMEBUFFER_BINDING, &eyebufferid);

                if(firstframe) {
                    mRenderTexture = new GVRRenderTexture(mGVRContext, 1024, 1024);
                    int myrendertextureid = mRenderTexture.getId();
                    mTexture = new GVRBitmapTexture(myrendertextureid);
                    sceneObject.getRenderData().getMaterial().setMainTexture(mTexture);
                }

                mRenderTexture.bind();
                    // make your gl calls
                glBindFramebuffer(GL_FRAMEBUFFER, eyebufferid);

        }
    } */

    @Override
    public void onStep() {
    }


    public void onTap() {
       pianoKeys.playMusic();
       drop.update();
       
    }
}


