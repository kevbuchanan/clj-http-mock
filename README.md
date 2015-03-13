# clj-http-mock

Mock http requests and responses for clj-http in speclj tests.

## Usage

```clj
(ns my-app.core-spec
  (:require [speclj.core :refer :all]
            [clj-http-mock :refer :all]))

(describe "my request"
  (it "makes a get request"
    (should-get "http://google.com" {:headers {"Authorization" "Basic 12345"}
                                     :response {:status 200 :body "Got it"}}
      (let [response [(clj-http/get "http://google.com" {:headers {"Authorization" "Basic 12345"}})]
        (should= 200 (:status response)
        (should= "Got it" (:body response)))))))
```
