package com.qhiehome.ihome.network.model.locklist;

/**
 * Created by YueMa on 2017/7/20.
 */

public class LockListResponse {

    /**
     * data : {"lock":{"id":123456789,"owner_id":123456789,"parking_id":123456789,"gateway_id":"xxxxxx","state":9}}
     * errcode : 0
     * errmsg : success
     */

    private DataBean data;
    private int errcode;
    private String errmsg;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public static class DataBean {
        /**
         * lock : {"id":123456789,"owner_id":123456789,"parking_id":123456789,"gateway_id":"xxxxxx","state":9}
         */

        private LockBean[] lock;

        public LockBean[] getLock() {
            return lock;
        }

        public void setLock(LockBean[] lock) {
            this.lock = lock;
        }

        public static class LockBean {
            /**
             * id : 123456789
             * owner_id : 123456789
             * parking_id : 123456789
             * gateway_id : xxxxxx
             * state : 9
             */

            private int id;
            private int owner_id;
            private int parking_id;
            private String gateway_id;
            private int state;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getOwner_id() {
                return owner_id;
            }

            public void setOwner_id(int owner_id) {
                this.owner_id = owner_id;
            }

            public int getParking_id() {
                return parking_id;
            }

            public void setParking_id(int parking_id) {
                this.parking_id = parking_id;
            }

            public String getGateway_id() {
                return gateway_id;
            }

            public void setGateway_id(String gateway_id) {
                this.gateway_id = gateway_id;
            }

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }
        }
    }
}
