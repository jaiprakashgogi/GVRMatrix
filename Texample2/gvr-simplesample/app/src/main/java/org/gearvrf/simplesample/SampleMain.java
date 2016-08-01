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
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;

public class SampleMain extends GVRScript {

    private GVRContext mGVRContext;
    private Context context;
    private GVRTexSceneObject texObject;
    private GLSurfaceView glView;
    private Texample2Renderer myRenderer;

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
                .setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera()
                .setBackgroundColor(Color.BLACK);
        // load texture
        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.gearvr_logo));
        GVRSceneObject sceneObject = new GVRSceneObject(gvrContext, 4.0f, 2.0f,
                texture);

        // set the scene object position
        sceneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);

        // add the scene object to the scene graph
     //s   scene.addSceneObject(sceneObject);

        GVRTexSceneObject myTex = new GVRTexSceneObject(gvrContext, 4, 2);
        myTex.getTransform().setPosition(0,0, -3.f);
        scene.addSceneObject(myTex);

        // gvrContext.registerOnDrawFrameListener(new myDrawFrameListener());

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



}


