package com.example.others;

public enum ResponseCode {
	BAD_REQUEST {
		public String toString() {
			return "400";
		}
	},
	INTERNAL_SERVER_ERROR {
		public String toString() {
			return "500";
		}
	},
	INIT {
		public String toString() {
			return "INIT";
		}
	},
	PROCESSED {
		public String toString() {
			return "PROCESSED";
		}
	}
}
