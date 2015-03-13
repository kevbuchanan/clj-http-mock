(ns clj-http-mock.core-spec
  (:require [speclj.core :refer :all :exclude [should-fail]]
            [clj-http-mock.core :refer :all]
            [clj-http.client :as clj-http]))

(defmacro should-fail
  [& form]
  `(try
     ~@form
     (throw (ex-info "Succeeded" {:success true}))
     (catch Exception e#
       (when (:success (ex-data e#))
         (-fail (format "Expected to fail but no failure was thrown")))
       (when-not (isa? (type e#) speclj.SpecFailure)
         (-fail (format "Expected a speclj failure but got %s" e#))))))

(describe "clj-http-mock"
  (context "speclj"
    (it "mocks a get request"
      (should-get "http://google.com" {:headers {"Authorization" "Basic 12345"}}
        (clj-http/get "http://google.com" {:headers {"Authorization" "Basic 12345"}})))

    (it "mocks a post request"
      (should-post "http://twitter.com" {:body "123" :headers {"Content-type" "application/json"}}
        (clj-http/post "http://twitter.com" {:body "123" :content-type :json})))

    (it "mocks a put request"
      (should-put "http://pizza.com" {:body "123" :headers {"Accept" "application/json"}}
        (clj-http/put "http://pizza.com" {:body "123" :accept :json})))

    (it "fails if it is not requested"
      (should-fail
        (should-get "http://google.com" {:headers {"Authorization" "Basic 12345"}}
          (+ 1 2))))

    (it "fails if the method does not match"
      (should-fail
        (should-get "http://google.com" {:headers {"Authorization" "Basic 12345"}}
          (clj-http/post "http://google.com" {:headers {"Authorization" "Basic 12345"}}))))

    (it "fails if the path does not match"
      (should-fail
        (should-get "http://goog.com" {:headers {"Authorization" "Basic 12345"}}
          (clj-http/get "http://google.com" {:headers {"Authorization" "Basic 12345"}}))))

    (it "fails if the expectd headers are not included in the request headers"
      (should-fail
        (should-get "http://google.com" {:headers {"Authorization" "Basic 12345"}}
          (clj-http/get "http://google.com" {}))))

    (it "fails if the request body does not match"
      (should-fail
        (should-post "http://google.com" {:body "expected body"}
          (clj-http/post "http://google.com" {:body "another body"}))))

    (it "returns a default 200 response"
      (should-get "http://google.com" {:headers {"Authorization" "Basic 12345"}}
        (let [response (clj-http/get "http://google.com" {:headers {"Authorization" "Basic 12345"}})]
          (should= 200 (:status response)))))

    (it "returns the provided response"
      (should-get "http://google.com" {:headers {"Authorization" "Basic 12345"}
                                       :response {:status 404 :body "Get out"}}
        (let [response (clj-http/get "http://google.com" {:headers {"Authorization" "Basic 12345"}
                                                          :throw-exceptions false})]
          (should= 404 (:status response))
          (should= "Get out" (:body response)))))))

