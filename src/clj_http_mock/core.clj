(ns clj-http-mock.core
  (:require [speclj.core :refer [-fail]]
            [clj-http.core :refer [request]]))

(defn- parse-request
  [{:keys [scheme server-name uri request-method body headers] :as req} expected-headers]
  {:req-method request-method
   :req-path (str (name scheme) "://" server-name uri)
   :req-body (when body (-> body .getContent slurp))
   :req-headers (select-keys headers (keys expected-headers))})

(defn fake-request
  [{:keys [method path headers body response] :or {headers {} response {}}} requested?]
  (fn [request]
    (let [{:keys [req-path req-method req-headers req-body]} (parse-request request headers)]
      (if (and (= method req-method)
               (= path req-path)
               (= body req-body)
               (= headers req-headers))
        (do
          (reset! requested? true)
          (merge {:status 200 :content-type :application/json}
                 (update-in response [:body] #(when % (.getBytes %)))))
        (-fail
          (format "Expected request: %s %s with body %s and headers %s \n but got request: %s %s with body %s and headers %s\n"
                  method path body headers req-method req-path req-body req-headers))))))

(defn- should-request
  [{:keys [method path headers body response] :as match-data} form]
  `(let [requested?# (atom false)]
     (with-redefs [clj-http.core/request (fake-request ~match-data requested?#)]
       ~@form
       (when-not @requested?#
         (-fail (format "%s %s with %s headers was not requested." ~method ~path, ~headers))))))

(defmacro should-get
  [path request-data & form]
  (should-request (assoc request-data :method :get :path path) form))

(defmacro should-post
  [path request-data & form]
  (should-request (assoc request-data :method :post :path path) form))

(defmacro should-put
  [path request-data & form]
  (should-request (assoc request-data :method :put :path path) form))
