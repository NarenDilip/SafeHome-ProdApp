package com.salzerproduct.safehome.webservice

import android.content.Context
import com.android.volley.Request
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.salzerproduct.constants.ApplicationConstants
import com.salzerproduct.http.*
import com.salzerproduct.safehome.AppPreference
import com.salzerproduct.safehome.BuildConfig
import com.salzerproduct.safehome.model.*
import org.json.JSONObject
import org.json.JSONTokener

/**
 * @since 24/2/17.<BR></BR>
 * Common request elements can be updated in this class.<BR></BR>
 * Extend this class for all web service classes to avoid redundancy
 */

open class ThingsManager {
    interface API {
        companion object {

            /* User and login APIs */
            const val login = "/api/auth/login"
            const val user = "/api/auth/user"
            const val saveUser = "/api/user"

            /* Device group and device APIs */
            const val saveEntityGroup = "/api/entityGroup" // One time before adding gateway
            const val getEntityGroupsForDevice = "/api/entityGroups/DEVICE"
            const val entitiesUnderGroup =
                "/api/entityGroup/{entityGroupId}/entities?pageSize=100&page=0"
            const val device = "/api/device" // GET for get device, POST for add Device
            const val enityGroup = "/api/entityGroup"
            const val getDeviceFromenityGroup =
                "/api/entityGroup"// GET for get device, POST for add Device
            const val getDeviceindexval =
                "/api/plugins/telemetry/DEVICE"// GET for get device, POST for add Device

            const val get_devicedetail = "/api/device"

            const val telemetryData = "/api/plugins/telemetry/DEVICE"
            const val getDeviceTelemetry = "/api/plugins/telemetry"

            const val getDeviceCredentialsByDeviceId = "/api/device/{deviceId}/credentials"
            const val saveDeviceCredential = "/api/device/credentials"
            const val rpcTwoWay = "/api/plugins/rpc/twoway/{deviceId}"
            const val rpcTwoWaygetStatus = "/api/plugins/rpc/twoway/{deviceId}"
            const val addAttribute =
                "/api/plugins/telemetry/{entityValType}/{entityId}/attributes/{scope}"
            const val getDeviceAttribute = "/api/plugins/telemetry"
            const val getDevicetelemetry = "/api/plugins/telemetry"
            const val saveRelation = "/api/relation"
            const val logout = "/api/auth/logout"
            const val getDeviceCurrentState =
                "/api/plugins/telemetry/DEVICE"
        }
    }

    companion object {

        private fun constructUrl(urlKey: String): String {
            return String.format("%s%s", ApplicationConstants.THINGS_BOARD_URL, urlKey)
        }

        private fun fillCommons(c: Context, r: VolleyClient) {
            r.addHeader(
                "X-Authorization",
                "Bearer ${AppPreference[c, AppPreference.Key.accessToken, ""]}"
            )
        }

        /**
         * @param c    Context of App
         * @param username - user given username
         * @param password - user given password
         * @return false if exception happened before http call
         */
        fun login(c: Context, username: String, password: String) {
            try {
                // Generating Req
                val client = JsonClient(
                    c, Request.Method.POST,
                    constructUrl(API.login), API.login.hashCode()
                )
                val jsonObject = JSONObject()
                jsonObject.put("username", username)
                jsonObject.put("password", password)
                client.execute(c as ResponseListener, jsonObject, LoginResponse::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun logout(c: Context, devicename: String) {
            try {
                // Generating Req
                val client = JsonClient(
                    c, Request.Method.POST,
                    constructUrl(API.logout), API.logout.hashCode()
                )
                val jsonObject = JSONObject()
                fillCommons(c = c, r = client)
                client.execute(c as ResponseListener, jsonObject, BaseRS::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun getUser(c: Context) {
            try {
                // Generating Req
                val client = RestClient(
                    c, Request.Method.GET,
                    constructUrl(API.user), API.user.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(c as ResponseListener, User::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

//        fun saveUser(c: Context, user: User) {
//            val fcmToken = FirebaseInstanceId.getInstance().token.toString()
//            if (!user.additionalInfo!!.registrationIds.contains(fcmToken)) {
//                user.additionalInfo!!.registrationIds.add(fcmToken)
//                val client = JsonClient(
//                    c, Request.Method.POST,
//                    constructUrl(API.saveUser), API.saveUser.hashCode()
//                )
//                fillCommons(c = c, r = client)
//                user.requestType = null
//                val jsonObject = JSONTokener(Gson().toJson(user)).nextValue() as JSONObject
//                client.execute(c as ResponseListener, jsonObject = jsonObject, responseType = User::class.java)
//            }
//        }

        fun saveEntityGroup(
            c: Context,
            groupName: String,
            description: String = "",
            displayName: String = ""
        ) {
            try {
                // Generating Req
                val client =
                    JsonClient(
                        c,
                        Request.Method.POST,
                        constructUrl(API.saveEntityGroup),
                        API.saveEntityGroup.hashCode()
                    )
                fillCommons(c = c, r = client)
                val jsonObject = JSONObject()
                jsonObject.put("name", groupName)
                jsonObject.put("type", "DEVICE")
                val descriptionJson = JSONObject()
                descriptionJson.put("description", description)
                descriptionJson.put("displayName", displayName)
                jsonObject.put("additionalInfo", descriptionJson)
                client.execute(
                    c as ResponseListener,
                    jsonObject = jsonObject,
                    responseType = Device::class.java
                )
            } catch (e: Exception) {
                throw e
            }
        }

        fun getEntityGroupsForDevice(c: Context) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.GET,
                    constructUrl(API.getEntityGroupsForDevice),
                    API.getEntityGroupsForDevice.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(
                    c as ResponseListener, responseType = Device::class.java
                )
            } catch (e: Exception) {
                throw e
            }
        }

        fun getdeviceindexval(c: Context, l: ResponseListener, deviceId: String) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.GET,
                    constructUrl("${API.getDeviceindexval}/$deviceId" + "/values/attributes?&keys=devIndex&keys=devLabel&keys=deviceuid&keys=devEditLabel&keys=type"),
                    API.getDeviceindexval.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(l, responseType = IndVal::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun getDevices(c: Context, l: ResponseListener, entityGroupId: String) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.GET,
                    constructUrl(API.entitiesUnderGroup.replace("{entityGroupId}", entityGroupId)),
                    API.entitiesUnderGroup.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(l, responseType = Device::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun getdevicecurrentState(
            c: Context,
            l: ResponseListener,
            deviceId: String,
            devicename: String
        ) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.GET,
                    constructUrl("${API.getDeviceCurrentState}/$deviceId" + "/values/attributes?&keys=state"),
                    API.getDeviceCurrentState.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(l, responseType = IndVal::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun getDevice(c: Context, l: ResponseListener, deviceId: String) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.GET,
                    constructUrl("${API.device}/$deviceId"),
                    API.device.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(l, responseType = Device::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun deleteDevice(c: Context, l: ResponseListener, deviceId: String) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.DELETE,
                    constructUrl("${API.device}/$deviceId"),
                    API.device.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(l, responseType = Device::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        //get logs of all device and sensors
        fun telemetryHistory(
            c: Context,
            l: ResponseListener,
            entityType: String,
            entityId: String,
            Keys: String,
            fromDate: String,
            toDate: String
        ) {
            try {
                //generate Request
                val client = RestClient(
                    c,
                    Request.Method.GET,
//                    constructUrl("${API.telemetryData}/$entityValType/$entityId/$Keys/$fromDate/$toDate/$interval/$limit/$agg"),
                    constructUrl("${API.telemetryData}/$entityId/values/timeseries?pageSize=100&page=0&agg=NONE&keys=alert" + "&startTs=$fromDate&endTs=" + "$toDate"),
                    API.telemetryData.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(l, responseType = History::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun deleteentityGroup(c: Context, l: ResponseListener, entityGroupId: String) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.DELETE,
                    constructUrl("${API.enityGroup}/$entityGroupId"),
                    API.enityGroup.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(l, responseType = Device::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun getdevicelist(c: Context, l: ResponseListener, entityGroupId: String) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.GET,
                    constructUrl("${API.getDeviceFromenityGroup}/$entityGroupId" + "/entities?pageSize=100&page=0"),
                    API.getDeviceFromenityGroup.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(l, responseType = Device::class.java)
            } catch (e: Exception) {
                throw e
            }
        }
//
//        fun getdeviceindexval(c: Context, l: ResponseListener, deviceId: String) {
//            try {
//                // Generating Req
//                val client = RestClient(
//                    c,
//                    Request.Method.GET,
//                    constructUrl("${API.getDeviceindexval}/$deviceId" + "/entities?limit=100&ascOrder=false"),
//                    API.getDeviceindexval.hashCode()
//                )
//                fillCommons(c = c, r = client)
//                client.execute(l, responseType = DevicedetailsInfo::class.java)
//            } catch (e: Exception) {
//                throw e
//            }
//        }

        fun getdevicedetails(c: Context, l: ResponseListener, deviceId: String) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.GET,
                    constructUrl("${API.get_devicedetail}/$deviceId"),
                    API.get_devicedetail.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(l, responseType = ThingsBoardResponse::class.java)
            } catch (e: Exception) {
                throw e
            }
        }


        fun getDeviceLatestAttributes(
            c: Context,
            l: ResponseListener,
            deviceId: String,
            entityType: String,
            Keys: String
        ) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.GET,
                    constructUrl("${API.getDeviceAttribute}/$entityType/$deviceId" + "/values/attributes?keys=$Keys"),
                    API.getDeviceAttribute.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(l, responseType = LatestTelemetryData::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun getDeviceLatestTelemetry(
            c: Context,
            l: ResponseListener,
            deviceId: String,
            entityType: String,
            Keys: String
        ) {
            try {
                val client = RestClient(
                    c,
                    Request.Method.GET,
                    constructUrl("${API.getDeviceTelemetry}/$entityType/$deviceId" + "/values/timeseries?keys=$Keys"),
                    API.getDeviceTelemetry.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(
                    l, responseType = History::class.java
                )
            } catch (e: Exception) {
                throw e
            }
        }

        fun saveDevice(
            c: Context,
            l: ResponseListener,
            entityGroupId: String,
            device: Device,
            extraOutput: String? = null
        ) {
            try {
                // Generating Req
                val client = JsonClient(
                    c,
                    Request.Method.POST,
                    constructUrl("${API.device}/?entityGroupId=$entityGroupId"),
                    API.device.hashCode()
                )
                fillCommons(c = c, r = client)
                client.extraOutput = extraOutput
                val jsonObject = JSONTokener(Gson().toJson(device)).nextValue() as JSONObject
                client.execute(
                    l = l,
                    jsonObject = jsonObject,
                    responseType = Device::class.java
                )
            } catch (e: Exception) {
                throw e
            }
        }

        fun saveRelation(c: Context, gatewayDeviceId: String, device: Device) {
            try {
                // Generating Req
                val client = JsonClient(
                    c, Request.Method.POST,
                    constructUrl(API.saveRelation), API.saveRelation.hashCode()
                )
                fillCommons(c = c, r = client)

                val fromObject = Entity()
                fromObject.entityType = "DEVICE"
                fromObject.id = gatewayDeviceId

                val jsonObject = JSONObject()
                jsonObject.put("type", "Contains")
                jsonObject.put("typeGroup", "COMMON")
                jsonObject.put(
                    "from",
                    JSONTokener(Gson().toJson(fromObject)).nextValue() as JSONObject
                )
                jsonObject.put(
                    "to",
                    JSONTokener(Gson().toJson(device.id)).nextValue() as JSONObject
                )
                client.execute(
                    c as ResponseListener,
                    jsonObject = jsonObject,
                    responseType = Response::class.java
                )
            } catch (e: Exception) {
                throw e
            }
        }

        fun getDeviceCredentialsByDeviceId(c: Context, deviceId: String) {
            try {
                // Generating Req
                val client = RestClient(
                    c,
                    Request.Method.GET,
                    constructUrl(
                        API.getDeviceCredentialsByDeviceId.replace(
                            "{deviceId}",
                            deviceId
                        )
                    ),
                    API.getDeviceCredentialsByDeviceId.hashCode()
                )
                fillCommons(c = c, r = client)
                client.execute(
                    c as ResponseListener,
                    responseType = DeviceCredential::class.java
                )
            } catch (e: Exception) {
                throw e
            }
        }

        fun saveDeviceCredential(c: Context, deviceCredential: DeviceCredential) {
            try {
                // Generating Req
                val client = JsonClient(
                    c, Request.Method.POST,
                    constructUrl(API.saveDeviceCredential), API.saveDeviceCredential.hashCode()
                )
                fillCommons(c = c, r = client)
                val jsonObject =
                    JSONTokener(Gson().toJson(deviceCredential)).nextValue() as JSONObject
                client.execute(
                    c as ResponseListener,
                    jsonObject = jsonObject,
                    responseType = DeviceCredential::class.java
                )
            } catch (e: Exception) {
                throw e
            }
        }

        /**
         * Arm / DisArm device using rpcTwoWay call
         */
        fun callRPCTwoWay(
            c: Context,
            l: ResponseListener,
            deviceId: String,
            jsonObject: JSONObject
        ) {
            try {
                // Generating Req
                val client = JsonClient(
                    c,
                    Request.Method.POST,
                    constructUrl(API.rpcTwoWay.replace("{deviceId}", deviceId)),
                    API.rpcTwoWay.hashCode()
                )
                client.extraOutput = jsonObject.toString()
                fillCommons(c = c, r = client)
                client.execute(l = l, jsonObject = jsonObject, responseType = Response::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        /**
         * Arm / DisArm device using rpcTwoWay call
         */
        fun callRPCTwoWayGetState(
            c: Context,
            l: ResponseListener,
            deviceId: String,
            jsonObject: JSONObject
        ) {
            try {
                // Generating Req
                val client = JsonClient(
                    c,
                    Request.Method.POST,
                    constructUrl(API.rpcTwoWaygetStatus.replace("{deviceId}", deviceId)),
                    API.rpcTwoWaygetStatus.hashCode()
                )
                client.extraOutput = jsonObject.toString()
                fillCommons(c = c, r = client)
                client.execute(l = l, jsonObject = jsonObject, responseType = Response::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        /**
         * Add fcm id attributes
         */
        fun addAttribute(c: Context, l: ResponseListener, deviceId: String) {
            try {
                // Generating Req`
                val client = JsonClient(
                    c, Request.Method.POST,
                    constructUrl(
                        API.addAttribute
                            .replace("{entityValType}", "DEVICE")
                            .replace("{entityId}", deviceId)
                            .replace("{scope}", "SHARED_SCOPE")
                    ), API.addAttribute.hashCode()
                )
                fillCommons(c = c, r = client)
                val jsonObject = JSONObject()
                jsonObject.put("fcmid", FirebaseInstanceId.getInstance().token)
                client.execute(l = l, jsonObject = jsonObject, responseType = Response::class.java)
            } catch (e: Exception) {
                throw e
            }
        }


        /**
         * Add deviceindex as attributes
         */
        fun addAttributedeviceindex(
            c: Context,
            l: ResponseListener,
            deviceId: String,
            deviceindex: String,
            devicename: String,
            devLabel: String,
            devicetoken: String,
            devicetype: String,
            devicestate: String
        ) {
            try {
                // Generating Req`
                val client = JsonClient(
                    c, Request.Method.POST,
                    constructUrl(
                        API.addAttribute
                            .replace("{entityValType}", "DEVICE")
                            .replace("{entityId}", deviceId)
                            .replace("{scope}", "SHARED_SCOPE")
                    ), API.addAttribute.hashCode()
                )
                fillCommons(c = c, r = client)
                val jsonObject = JSONObject()
                jsonObject.put("devIndex", deviceindex)
                jsonObject.put("devLabel", devicename)
                jsonObject.put("devEditLabel", devLabel)
                jsonObject.put("deviceuid", devicetoken)
                jsonObject.put("type", devicetype)
                if (devicetype == "gw") {
                    jsonObject.put("state", devicestate)
                }
                client.execute(l = l, jsonObject = jsonObject, responseType = Response::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        /**
         * Add devicename as attributes
         */
        fun addAttributedevicename(
            c: Context,
            l: ResponseListener,
            deviceId: String,
            devicename: String
        ) {
            try {
                // Generating Req`
                val client = JsonClient(
                    c, Request.Method.POST,
                    constructUrl(
                        API.addAttribute
                            .replace("{entityValType}", "DEVICE")
                            .replace("{entityId}", deviceId)
                            .replace("{scope}", "SHARED_SCOPE")
                    ), API.addAttribute.hashCode()
                )
                fillCommons(c = c, r = client)
                val jsonObject = JSONObject()
                jsonObject.put("devLabel", devicename)
                client.execute(l = l, jsonObject = jsonObject, responseType = Response::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun addAttributeEditdevicename(
            c: Context,
            l: ResponseListener,
            deviceId: String,
            devLabel: String
        ) {
            try {
                // Generating Req`df
                val client = JsonClient(
                    c, Request.Method.POST,
                    constructUrl(
                        API.addAttribute
                            .replace("{entityValType}", "DEVICE")
                            .replace("{entityId}", deviceId)
                            .replace("{scope}", "SHARED_SCOPE")
                    ), API.addAttribute.hashCode()
                )
                fillCommons(c = c, r = client)
                val jsonObject = JSONObject()
                jsonObject.put("devEditLabel", devLabel)
                client.execute(l = l, jsonObject = jsonObject, responseType = Response::class.java)
            } catch (e: Exception) {
                throw e
            }
        }

        fun addAttributedeviceuid(
            c: Context,
            l: ResponseListener,
            deviceId: String,
            devicename: String
        ) {
            try {
                // Generating Req`
                val client = JsonClient(
                    c, Request.Method.POST,
                    constructUrl(
                        API.addAttribute
                            .replace("{entityValType}", "DEVICE")
                            .replace("{entityId}", deviceId)
                            .replace("{scope}", "SHARED_SCOPE")
                    ), API.addAttribute.hashCode()
                )
                fillCommons(c = c, r = client)
                val jsonObject = JSONObject()
                jsonObject.put("deviceuid", devicename)
                client.execute(l = l, jsonObject = jsonObject, responseType = Response::class.java)
            } catch (e: Exception) {
                throw e
            }
        }


        /**
         * Add deviceindex as attributes
         */
//        fun addAttributedevicestatus(c: Context, l: ResponseListener, deviceId: String, armstate: Boolean) {
//            try {
//                // Generating Req`
//                val client = JsonClient(
//                    c, Request.Method.POST,
//                    constructUrl(
//                        API.addAttribute
//                            .replace("{entityValType}", "DEVICE")
//                            .replace("{entityId}", deviceId)
//                            .replace("{scope}", "SHARED_SCOPE")
//                    )
//                    , API.addAttribute.hashCode()
//                )
//                fillCommons(c = c, r = client)
//                val jsonObject = JSONObject()
//                jsonObject.put("armstate", armstate)
//                client.execute(l = l, jsonObject = jsonObject, responseType = Response::class.java)
//            } catch (e: Exception) {
//                throw e
//            }
//        }

    }
}