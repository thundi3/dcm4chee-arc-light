/*
 * **** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4chee.arc.ups.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4chee.arc.conf.ArchiveAEExtension;
import org.dcm4chee.arc.entity.Workitem;
import org.dcm4chee.arc.entity.Workitem_;
import org.dcm4chee.arc.keycloak.HttpServletRequestInfo;
import org.dcm4chee.arc.ups.UPSContext;
import org.dcm4chee.arc.ups.UPSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Sep 2019
 */
@ApplicationScoped
public class UPSServiceImpl implements UPSService {

    private static Logger LOG = LoggerFactory.getLogger(UPSServiceImpl.class);

    @Inject
    private UPSServiceEJB ejb;

    @Override
    public UPSContext newUPSContext(HttpServletRequestInfo httpRequestInfo, ArchiveAEExtension arcAE) {
        return new UPSContextImpl(httpRequestInfo, arcAE);
    }

    @Override
    public boolean createWorkitem(UPSContext ctx) {
        return ejb.createWorkitem(ctx);
    }

    @Override
    public boolean getWorkitem(UPSContext ctx) {
        Workitem workitem = ejb.getWorkitem(ctx);
        if (workitem == null) {
            return false;
        }
        Attributes upsAttrs = workitem.getAttributes();
        Attributes patAttrs = workitem.getPatient().getAttributes();
        Attributes.unifyCharacterSets(patAttrs, upsAttrs);
        Attributes attrs = new Attributes(patAttrs.size() + upsAttrs.size() + 3);
        attrs.addAll(patAttrs);
        attrs.addAll(upsAttrs);
        attrs.setString(Tag.SOPClassUID, VR.UI, UID.UnifiedProcedureStepPushSOPClass);
        attrs.setString(Tag.SOPInstanceUID, VR.UI, workitem.getSopInstanceUID());
        attrs.setDate(Tag.ScheduledProcedureStepModificationDateTime, VR.DT, workitem.getUpdatedTime());
        ctx.setAttributes(attrs);
        return true;
    }
}
