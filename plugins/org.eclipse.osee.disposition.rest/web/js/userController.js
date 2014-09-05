app.controller('userController', [
    '$scope',
    '$modal',
    '$rootScope',
    '$cookieStore',
    'Program',
    'Set',
    'Item',
    'Annotation',

    function($scope, $modal, $rootScope, $cookieStore, Program, Set, Item, Annotation) {
    	$scope.unselectingItem = false;
    	$scope.editItems = false;
    	$scope.selectedItems = [];
        $scope.programSelection = null;
        $scope.setSelection = null; 	
        $scope.lastFocused = null;

        // Get programs
        Program.query(function(data) {
            $scope.programs = data;
        });

        $scope.updateProgram = function updateProgram() {
            Set.query({
                programId: $scope.programSelection
            }, function(data) {
                $scope.sets = data;
            });
        };
        
        $scope.updateSet = function updateSet() {
            Item.query({
                programId: $scope.programSelection,
                setId: $scope.setSelection
            }, function(data) {
                $scope.items = data;
            });

            Set.get({
                programId: $scope.programSelection,
                setId: $scope.setSelection
            }, function(data) {
                $scope.set = data;
                $scope.dispoConfig = $scope.set.dispoConfig;
            });
        };

        $scope.updateItem = function updateItem(item, row) {
        	$scope.unselectingItem = true;
        	$scope.gridOptions.selectAll(false);
        	$scope.unselectingItem = false;
            $scope.selectedItem = item;
            Annotation.query({
                programId: $scope.programSelection,
                setId: $scope.setSelection,
                itemId: item.guid
            }, function(data) {
                $scope.annotations = data;
                var blankAnnotation = new Annotation();
                $scope.annotations.push(blankAnnotation);

                $scope.gridOptions.selectRow(row.rowIndex, true);
            });


        };

        $scope.$on('ngGridEventStartCellEdit', function(data) {
            var field = data.targetScope.col.field;
            $scope.cachedValue = data.targetScope.row.getProperty(field);
        });

        $scope.$on('ngGridEventEndCellEdit', function(data) {
            var field = data.targetScope.col.field;
            var newValue = data.targetScope.row.getProperty(field);

            if ($scope.cachedValue != newValue) {
                var object = data.targetScope.row.entity;
                $scope.editItem(data.targetScope.row.entity);
            }
        });
        
        $scope.toggleEditItems = function toggleEditItems() {
        	var size = $scope.selectedItems.length;
        	$scope.gridOptions.selectAll(false);
        	// Why do this last? Good question, checkSeletable gets called by selectAll and needs editItems to be true so ng-grid can properly unselect the selected items withouth breaking it's 'watch' function
        	$scope.editItems = !$scope.editItems;
        	$scope.annotations.length = 0;
        }
        
        // Need this so that user clicks on grid rows doesn't automatically change the selected/highlighted row
        var checkSelectable = function checkSelectable(data) {
        	if($scope.editItems || $scope.unselectingItem){
        		return true;
        	} else {
        		return false;
        	}
        };

        var checkboxSorting = function checkboxSorting(itemA, itemB) {
            if (itemA.needsRerun == itemA.needsRerun) {
                return itemA;
            } else if (itemA.needsRerun) {
                return itemA;
            } else if (itemB.needsRerun) {
                return itemB;
            } else {
                return itemA;
            }
        };

        var origCellTmpl = '<div ng-dblclick="updateItem(row.entity, row)">{{row.entity.name}}</div>';
        var dupCellTmpl = '<button class="btn btn-default btn-sm" ng-dblclick="updateItem(row.entity)">{{row.getProperty(col.field)}}</button>';
        var chkBoxTemplate = '<input type="checkbox" class="form-control" ng-model="COL_FIELD" ng-change="editItem(row.entity)">hello world</input>';
        var assigneeCellTmpl = '<div ng-dblclick="stealItem(row.entity)">{{row.entity.assignee}}</div>';
        var statusCellTem = '<div ng-class="{ItemStatus: true, pass: row.entity.status == \'PASS\', incomplete: row.entity.status == \'INCOMPLETE\', complete: row.entity.status == \'COMPLETE\'}">{{row.entity.status}}</div>';

        $scope.gridOptions = {
            data: 'items',
            enableHighlighting: true,
            enableColumnResize: true,
            enableRowReordering: true,
            multiSelect: true,
            showFilter: true,
            showColumnMenu: true,
            selectedItems: $scope.selectedItems,
            beforeSelectionChange: checkSelectable,
            columnDefs: [{
                field: 'name',
                displayName: 'Name',
                width: 400,
                cellTemplate: origCellTmpl
            }, {
                field: 'status',
                displayName: 'Status',
                width: 150,
            }, {
                field: 'totalPoints',
                displayName: 'Total',
                width: 100
            }, {
                field: 'failureCount',
                displayName: 'Failure Count',
                width: 125
            }, {
                field: 'discrepanciesAsRanges',
                displayName: 'Failed Points',
                width: 400
            }, {
                field: 'assignee',
                displayName: 'Assignee',
                width: 200,
                enableCellEdit: false,
                cellTemplate: assigneeCellTmpl
            }, {
                field: 'needsRerun',
                displayName: 'Rerun?',
                enableCellEdit: false,
                cellTemplate: chkBoxTemplate,
                sortFn: checkboxSorting
            }, {
                field: 'category',
                displayName: 'Category',
                enableCellEdit: true,
                visible: false
            }, {
                field: 'machine',
                displayName: 'Station',
                enableCellEdit: true,
                visible: false
            }, {
                field: 'elapsedTime',
                displayName: 'Elapsed Time',
                enableCellEdit: false,
                visible: false
            }, {
                field: 'aborted',
                displayName: 'Aborted',
                enableCellEdit: false,
                visible: false
            }]

        };
        
        $scope.saveLastFocused = function saveLastFocused(element) {
            $scope.lastFocused = element;
        }
        
        $scope.stealItem = function stealItem(item) {
            if ($rootScope.cachedName != null) {
                if ($rootScope.cachedName != item.assignee) {
                    var confirmed = window.confirm("Are you sure you want to steal this Item from " + item.assignee);
                    if (confirmed) {
                        item.assignee = $rootScope.cachedName;
                        $scope.editItem(item);
                    }
                }
            }
        }

        $scope.resolutionTypes = [{
            text: "Code",
            value: "CODE"
        }, {
            text: "Test",
            value: "TEST"
        }, {
            text: "Requirement",
            value: "REQUIREMENT"
        }, {
            text: "Other",
            value: "OTHER"
        }, {
            text: "Undetermined",
            value: "UNDETERMINED"
        }];

        $scope.deleteAnnotation = function deleteAnnotation(annotation) {
            Annotation.delete({
                programId: $scope.programSelection,
                setId: $scope.setSelection,
                itemId: $scope.selectedItem.guid,
                annotationId: annotation.guid,
                userName: $rootScope.cachedName,
            }, function() {
                var index = $scope.annotations.indexOf(annotation);
                if (index > -1) {
                    $scope.annotations.splice(index, 1);
                }
                Item.get({
                    programId: $scope.programSelection,
                    setId: $scope.setSelection,
                    itemId: $scope.selectedItem.guid
                }, function(data) {
                    $scope.selectedItem.status = data.status;
                });
            }, function(data) {
                alert("Could not make change, please try refreshing");
            });

        }

        $scope.editItem = function editItem(item) {
            Item.update({
                programId: $scope.programSelection,
                setId: $scope.setSelection,
                itemId: item.guid,
            }, item, function() {}, function(data) {
                alert("Could not make change, please try refreshing");
            });

        }

        $scope.getInvalidLocRefs = function getInvalidLocRefs(annotation) {
            return !annotation.isConnected && annotation.locationRefs != null;
        }

        $scope.getInvalidRes = function getInvalidRes(annotation) {
            return annotation.resolution != null && annotation.resolution != "" && !annotation.isResolutionValid;
        }

        $scope.editAnnotation = function editAnnotation(annotation) {
        	$scope.lastFocused;
            if (annotation.guid == null) {
                $scope.createAnnotation(annotation);
            } else {
                Annotation.update({
                    programId: $scope.programSelection,
                    setId: $scope.setSelection,
                    itemId: $scope.selectedItem.guid,
                    annotationId: annotation.guid,
                    userName: $rootScope.cachedName,
                }, annotation, function(annot) {
                    // get latest Annotation version from Server
                    Annotation.get({
                        programId: $scope.programSelection,
                        setId: $scope.setSelection,
                        itemId: $scope.selectedItem.guid,
                        annotationId: annotation.guid
                    }, function(data) {
                        annotation.isConnected = data.isConnected;
                        annotation.isResolutionValid = data.isResolutionValid;
                    });

                    // Get new latest Item version from server
                    Item.get({
                        programId: $scope.programSelection,
                        setId: $scope.setSelection,
                        itemId: $scope.selectedItem.guid
                    }, function(data) {
                        $scope.selectedItem.status = data.status;
                    });
                }, function(data) {
                    alert("Could not make change, please try refreshing");
                });
            }
        }

        $scope.createAnnotation = function createAnnotation(annotation) {
            annotation.$save({
                programId: $scope.programSelection,
                setId: $scope.setSelection,
                itemId: $scope.selectedItem.guid,
                userName: $rootScope.cachedName,
            }, function() {
            	var nextFocused = $scope.lastFocused.$$prevSibling;
            	nextFocused.focus;
            	nextFocused.focusMe;
            	
                Item.get({
                    programId: $scope.programSelection,
                    setId: $scope.setSelection,
                    itemId: $scope.selectedItem.guid
                }, function(data) {
                    $scope.selectedItem.status = data.status;
                });

                var blankAnnotation = new Annotation();
                $scope.annotations.push(blankAnnotation);
            }, function(data) {
                alert("Could not make change, please try refreshing");
            });
        }

        $scope.expandAnnotations = function expandAnnotations(item) {
            $scope.selectedItem = item;
            if (item.annotations == null) {
                Annotation.query({
                    programId: $scope.programSelection,
                    setId: $scope.setSelection,
                    itemId: item.guid
                }, function(data) {
                    item.annotations = data;

                    var blankAnnotation = new Annotation;
                    item.annotations.push(blankAnnotation);
                    item.showDetails = (data.length > 0);
                });
            } else if (!item.showDetails) {
                item.showDetails = (item.annotations.length > 0);
            } else {
                item.showDetails = false;
            }
        };

        $scope.toggleDetails = function toggleDetails(annotation) {
            if (annotation.showDeets == null) {
                annotation.showDeets = true;
            } else if (annotation.showDeets) {
                annotation.showDeets = false;
            } else {
                annotation.showDeets = true;
            }
        }
        
        
        // MODALS -------------------------------------------------------------------------------------------------
        $scope.showAssigneeModal = function() {
            var modalInstance = $modal.open({
                templateUrl: 'assigneeModal.html',
                controller: AssigneeModalCtrl,
                size: 'sm',
                windowClass: 'assigneeModal'
            });

            modalInstance.result.then(function(inputs) {
            	var size = $scope.selectedItems.length;
            	for(var i = 0; i < size; i++) {
            		if($scope.selectedItems[i].assignee != inputs.multiAssignee){
	            		$scope.selectedItems[i].assignee = inputs.multiAssignee;
	            		$scope.editItem($scope.selectedItems[i]);
            		}
            	}
            });
        }
        
        var AssigneeModalCtrl = function($scope, $modalInstance) {
            $scope.multiAssignee = "";

            $scope.ok = function() {
                var inputs = {};
                inputs.multiAssignee = this.multiAssignee;
                $modalInstance.close(inputs);
            };

            $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
            };
        };
        
        // Category Modal
        $scope.showCategoryModal = function() {
            var modalInstance = $modal.open({
                templateUrl: 'categoryModal.html',
                controller: CategoryModalCtrl,
                size: 'sm',
                windowClass: 'categoryModal'
            });

            modalInstance.result.then(function(inputs) {
            	var size = $scope.selectedItems.length;
            	for(var i = 0; i < size; i++) {
            		if($scope.selectedItems[i].category != inputs.category){
	            		$scope.selectedItems[i].category = inputs.category;
	            		$scope.editItem($scope.selectedItems[i]);
            		}
            	}
            });
        }
        
        var CategoryModalCtrl = function($scope, $modalInstance) {
            $scope.multiCategory = "";

            $scope.ok = function() {
                var inputs = {};
                inputs.category = this.multiCategory;
                $modalInstance.close(inputs);
            };

            $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
            };
        };
        
        // Needs Rerun Modal
        $scope.showNeedsRerunModal = function() {
            var modalInstance = $modal.open({
                templateUrl: 'needsRerunModal.html',
                controller: NeedsRerunModalCtrl,
                size: 'sm',
                windowClass: 'needsRerunModal'
            });

            modalInstance.result.then(function(inputs) {
            	var size = $scope.selectedItems.length;
            	for(var i = 0; i < size; i++) {
            		if($scope.selectedItems[i].needsRerun != inputs.needsRerun) {
            			$scope.selectedItems[i].needsRerun = inputs.needsRerun;
            			$scope.editItem($scope.selectedItems[i]);
            		}
            	}
            });
        }
        
        var NeedsRerunModalCtrl = function($scope, $modalInstance) {
            $scope.ok = function() {
                var inputs = {};
                if(this.formData == undefined) {
                	inputs.needsRerun = true;
                } else {
                	inputs.needsRerun = this.formData.multiNeedsRerun;
                }
                $modalInstance.close(inputs);
            };

            $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
            };
        };


    }
]);

//http://stackoverflow.com/questions/11868393/angularjs-inputtext-ngchange-fires-while-the-value-is-changing
app.directive('ngModelOnblur', function() {
    return {
        restrict: 'A',
        require: 'ngModel',
        priority: 1, // needed for angular 1.2.x
        link: function(scope, elm, attr, ngModelCtrl) {
            if (attr.type === 'radio' || attr.type === 'checkbox') return;

            elm.unbind('input').unbind('keydown').unbind('change');
            elm.bind('blur', function() {
                scope.$apply(function() {
                    ngModelCtrl.$setViewValue(elm.val());
                });
            });
        }
    };
});

// http://stackoverflow.com/questions/18398472/disabled-text-box-accessible-using-tab-key
app.directive('focusMe', function($timeout) {
	  return {
	    scope: { trigger: '=focusMe' },
	    link: function(scope, element) {
	      scope.$watch('trigger', function(value) {
	        if(value === true) { 
	            element[0].focus();
	        }
	      });
	    }
	  };
	});
