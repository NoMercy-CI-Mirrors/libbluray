/*
 * This file is part of libbluray
 * Copyright (C) 2010  William Hahne
 * Copyright (C) 2019  Petri Hintukainen <phintuka@users.sourceforge.net>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.havi.ui;

import java.util.ArrayList;

import org.blurayx.s3d.ui.HVideoConfigTemplateS3D;
import org.blurayx.uhd.ui.HVideoConfigurationTemplateUHD;

public class HVideoDevice extends HScreenDevice {
    protected HVideoDevice() {
        boolean is_p6 = isProfile6();
        boolean is_p5 = isProfile5();
        int length = HScreenConfigTemplate.defaultConfig.length;
        hvcArray = new HVideoConfiguration[length];
        for (int i = 0; i < length; i++) {
            HVideoConfigTemplate hvct;
            if (is_p6) {
                hvct = new HVideoConfigurationTemplateUHD();
            } else if (is_p5) {
                hvct = new HVideoConfigTemplateS3D();
            } else {
                hvct = new HVideoConfigTemplate();
            }
            HScreenConfigTemplate.initDefaultConfigTemplate(hvct, i);
            hvcArray[i] = new HVideoConfiguration(hvct);
        }
        hvc = hvcArray[0];
        s3dArray = new ArrayList[length];
    }

    public HVideoConfiguration[] getConfigurations() {
        return hvcArray;
    }

    public HVideoConfiguration getDefaultConfiguration() {
        return hvcArray[0];
    }

    public HVideoConfiguration getBestConfiguration(HVideoConfigTemplate hvct) {
        int score = -1;
        HVideoConfiguration hvc = null;
        for (int i = 0; i < hvcArray.length; i++)
            if (hvct.match(hvcArray[i]) > score)
                hvc = hvcArray[i];
        return getS3DConfiguration(hvc, hvct);
    }

    public HVideoConfiguration getBestConfiguration(HVideoConfigTemplate hvcta[]) {
        int score = -1;
        HVideoConfiguration hvc = null;
        HVideoConfigTemplate hvct = null;
        for (int i = 0; i < hvcArray.length; i++) 
            for (int j = 0; j < hvcta.length; j++)
                if (hvcta[j].match(hvcArray[i]) > score) {
                    hvc = hvcArray[i];
                    hvct = hvcta[j];
                }
        return getS3DConfiguration(hvc, hvct);
    }

    /*
     * Profile 5: if the template asks for a stereoscopic mode (HVideoConfigTemplateS3D.S3D),
     * return a configuration whose template reports that mode.
     * Applications set the configuration and then wait until
     * getCurrentConfiguration().getConfigTemplate().getPreferenceObject(S3D) reports it.
     */
    private synchronized HVideoConfiguration getS3DConfiguration(HVideoConfiguration hvc, HVideoConfigTemplate hvct) {
        if (hvc == null || !(hvct instanceof HVideoConfigTemplateS3D) ||
            !(hvc.getConfigTemplate() instanceof HVideoConfigTemplateS3D))
            return hvc;

        Object s3d = hvct.getPreferenceObject(HVideoConfigTemplateS3D.S3D);
        int priority = hvct.getPreferencePriority(HVideoConfigTemplateS3D.S3D);
        if (s3d == null ||
            (priority != HScreenConfigTemplate.REQUIRED && priority != HScreenConfigTemplate.PREFERRED))
            return hvc;

        int index;
        for (index = 0; hvcArray[index] != hvc; index++)
            ;
        if (s3dArray[index] == null)
            s3dArray[index] = new ArrayList();

        for (int i = 0; i < s3dArray[index].size(); i++) {
            HVideoConfiguration s3dHvc = (HVideoConfiguration)s3dArray[index].get(i);
            if (s3d.equals(s3dHvc.getConfigTemplate().getPreferenceObject(HVideoConfigTemplateS3D.S3D)))
                return s3dHvc;
        }

        HVideoConfigTemplateS3D s3dHvct = new HVideoConfigTemplateS3D();
        HScreenConfigTemplate.initDefaultConfigTemplate(s3dHvct, index);
        s3dHvct.setPreference(HVideoConfigTemplateS3D.S3D, s3d, HScreenConfigTemplate.REQUIRED);
        HVideoConfiguration s3dHvc = new HVideoConfiguration(s3dHvct);
        s3dArray[index].add(s3dHvc);
        return s3dHvc;
    }

    public HVideoConfiguration getCurrentConfiguration() {
        return hvc;
    }

    public boolean setVideoConfiguration(HVideoConfiguration hvc)
            throws SecurityException, HPermissionDeniedException, HConfigurationException {
        this.hvc = hvc;
        return true;
    }

    public Object getVideoSource() throws SecurityException, HPermissionDeniedException {
        org.videolan.Logger.unimplemented(HVideoDevice.class.getName(), "getVideoSource");
        throw new HPermissionDeniedException();
    }

    public Object getVideoController() throws SecurityException, HPermissionDeniedException {
        org.videolan.Logger.unimplemented(HVideoDevice.class.getName(), "getVideoController");
        throw new HPermissionDeniedException();
    }

    public static final HVideoConfiguration NOT_CONTRIBUTING = null;
    private HVideoConfiguration[] hvcArray;
    private ArrayList[] s3dArray;
    private HVideoConfiguration hvc;
}
