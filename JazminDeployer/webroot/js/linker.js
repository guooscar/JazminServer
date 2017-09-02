/*!
 * Linker, Node Editor Library v0.0.1
 * https://github.com/m-reda/linker
 *
 *
 * Released under the MIT license
 *
 * Date: 2017-03-19
 */

(function ($) {
    $.fn.linker = function (options) {
        var lk = $("<div class=\"linker_board\"><svg id=\"linker_paths\"></svg></div>").appendTo(this),
            settings = $.extend({settingIcon: true}, options),
            container = $(this).addClass("linker_container"),
            idCounter = Date.now(),
            settingIcon = "<svg class=\"setting\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 54 54\" xml:space=\"preserve\"><path d=\"M51.22,21h-5.052c-0.812,0-1.481-0.447-1.792-1.197s-0.153-1.54,0.42-2.114l3.572-3.571 c0.525-0.525,0.814-1.224,0.814-1.966c0-0.743-0.289-1.441-0.814-1.967l-4.553-4.553c-1.05-1.05-2.881-1.052-3.933,0l-3.571,3.571 c-0.475,0.475-0.997,0.574-1.352,0.574c-0.5,0-0.997-0.196-1.364-0.539C33.324,8.984,33,8.534,33,7.832V2.78 C33,1.247,31.753,0,30.22,0H23.78C22.247,0,21,1.247,21,2.78v5.052c0,1.218-0.997,1.945-1.961,1.945c-0.354,0-0.876-0.1-1.351-0.574 l-3.571-3.571c-1.052-1.052-2.883-1.05-3.933,0l-4.553,4.553c-0.525,0.525-0.814,1.224-0.814,1.967c0,0.742,0.289,1.44,0.814,1.966 l3.572,3.571c0.573,0.574,0.73,1.364,0.42,2.114S8.644,21,7.832,21H2.78C1.247,21,0,22.247,0,23.78v6.438C0,31.752,1.247,33,2.78,33 h5.052c0.812,0,1.481,0.447,1.792,1.197s0.153,1.54-0.42,2.114l-3.572,3.571c-0.525,0.525-0.814,1.224-0.814,1.966 c0,0.743,0.289,1.441,0.814,1.967l4.553,4.553c1.051,1.051,2.881,1.053,3.933,0l3.571-3.571c0.475-0.475,0.997-0.574,1.352-0.574 c0.963,0,1.96,0.728,1.96,1.945v5.051C21,52.752,22.247,54,23.78,54h6.439c1.533,0,2.78-1.248,2.78-2.781v-5.051 c0-1.218,0.997-1.945,1.96-1.945c0.354,0,0.877,0.1,1.352,0.574l3.571,3.571c1.052,1.052,2.883,1.05,3.933,0l4.553-4.553 c0.525-0.525,0.814-1.224,0.814-1.967c0-0.742-0.289-1.44-0.814-1.966l-3.572-3.571c-0.573-0.574-0.73-1.364-0.42-2.114 S45.356,33,46.168,33h5.052c1.533,0,2.78-1.248,2.78-2.781V23.78C54,22.247,52.753,21,51.22,21z M34,27c0,3.859-3.141,7-7,7 s-7-3.141-7-7s3.141-7,7-7S34,23.141,34,27z\"/><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g><g></g></svg>";

        // generate random and unique id
        function gid() {
            idCounter++;
            return Math.random().toString(36) + idCounter;
        }

        /** Curve
         *  calculate the path curve
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         * @returns {string}
         */
        function curve(x1, y1, x2, y2) {
            var offset = lk.offset();
            x1 -= offset.left;
            y1 -= offset.top;
            x2 -= offset.left;
            y2 -= offset.top;

            var d = Math.abs(x1 - x2) / 2;
            y1 += 5;
            y2 += 5;

            return " M" + x1 + "," + y1 +      // start
                " C" + (x1 + d) + "," + y1 +      // control 1
                " " + (x2 - d) + "," + y2 +      // control 2
                " " + x2 + "," + y2 +      // end
                " " + "l-1 0 l-5 -5 m5 5 l-5 5"; // arrow
        }

        /** Draw Path
         *
         * @param p1
         * @param p2
         * @returns {Element}
         */
        function drawPath(p1, p2) {
            var p = document.createElementNS("http://www.w3.org/2000/svg", "path");
            p.setAttribute("d", curve(p1.left, p1.top, p2.left, p2.top));
            document.getElementById("linker_paths").appendChild(p);

            return p;
        }

        /** Node
         *
         * @param data
         * @returns {{x: number, y: number}}
         */
        this.node = function (data) {
            var node = data ? data : {x: 0, y: 0};
            var _nodeClass = "";
            if (typeof(data.type) === "string") {
                _nodeClass = "linker_node_" + data.type;
            }
            node.__id = gid();
            node.id = node.id ? node.id : node.__id;
            node.el = $("<div class=\"linker_node node_" + node.id + " " + _nodeClass + "\" style=\"left:" + node.x + "px;top: " + node.y + "px;\"><h3><div class='linker_node_title'>" + node.name + "</div><span class=\"remove\"></span></h3><div class=\"linker_inputs\"></div><div class=\"linker_outputs\"></div></div>");
            node.el.data("obj", node);
            node.pathsOut = {}; // paths out from this node
            node.pathsIn = {}; // paths in to this node
            // remove node
            node.el.click(function (event) {
                event.preventDefault();
                var $this = $(this);
                if ($this.hasClass("active")) {
                    return;
                }
                $(".linker_node.active").removeClass("active");
                $this.addClass("active");
                if (node.onActive) {
                    node.onActive();
                }
            });
            $("h3 .remove", node.el).click(function () {
                if (!confirm("Delete this node?")) {
                    return;
                }
                node.el.remove();
                // remove the out connections
                $.each(node.pathsOut, function (_, arr) {
                    $.each(arr, function (_, p) {
                        $(p[0]).remove();
                    });
                });
                // remove the in connections
                $.each(node.pathsIn, function (_, arr) {
                    $.each(arr, function (_, p) {
                        $(p[0]).trigger("click", true);
                    });
                });
                // trigger the remove event
                if (node.onRemove) {
                    node.onRemove();
                }
            });

            // add input to the node
            node.inputs = [];
            node.name = function (name) {
                if (typeof(name) !== "string") {
                    return node.el.find("div.linker_node_title").text();
                }
                node.el.find("div.linker_node_title").text(name);
            };
            node.input = function (id, name) {
                var i = node.inputs.push({
                    __id: gid(),
                    id: id,
                    name: name,
                    node: node,
                    el: $("<div class=\"linker_point\" data-type=\"input\"></div>")
                });

                var input = node.inputs[i - 1];
                input.el.data("obj", input);
                node.pathsIn[input.__id] = [];


                var label = $("<div class=\"linker_label\"><span>" + name + "</span></span></div>").append(input.el);
                $(".linker_inputs", node.el).append(label);
                return input;
            };

            // add output to the node
            node.outputs = [];
            node.output = function (id, name) {
                var i = node.outputs.push({
                    __id: gid(),
                    id: id,
                    name: name,
                    node: node,
                    el: $("<div class=\"linker_point\" data-type=\"output\"></div>")
                });

                var output = node.outputs[i - 1];
                output.el.data("obj", output);
                node.pathsOut[output.__id] = [];

                output.connect = function (input, withoutEvent) {
                    var nodeOuts = node.pathsOut[this.__id];

                    // check if connection exist
                    for (var i = 0; i < nodeOuts.length; i++) {
                        if (input.__id === nodeOuts[i][2].__id) {
                            return;
                        }
                    }

                    var path = drawPath(this.el.offset(), input.el.offset()),
                        conn = [path, this, input];

                    // append the connection to node's outs and the other node's ins
                    nodeOuts.push(conn);
                    input.node.pathsIn[input.__id].push(conn);

                    if (!withoutEvent && this.onConnect) {
                        this.onConnect(input);
                    }
                    // remove connection
                    $(path).on("click", function (e, silence) {
                        if (!silence) {
                            if (!confirm("Delete this path?")) {
                                return;
                            }
                        }
                        var outIdx = node.pathsOut[output.__id].indexOf(conn);
                        node.pathsOut[output.__id].splice(outIdx, 1);
                        input.node.pathsIn[input.__id].splice(1, 1);
                        $(this).remove();

                        if (output.onRemove) {
                            output.onRemove(input, outIdx);
                        }
                    });
                };

                var label = $("<div class=\"linker_label\"><span>" + name + "</span></div>").append(output.el);
                $(".linker_outputs", node.el).append(label);
                return output;
            };

            // node setting
            if (settings.settingIcon) {
                $(settingIcon).appendTo($("h3", node.el)).click(function () {
                    if (node.onSetting) {
                        node.onSetting();
                    }
                });
            }
            // add the node to the linker container
            lk.append(node.el);

            return node;
        };


        /*
         *  linking
         */
        var selectedOutput, dragPath, dragPathPos;
        lk.on("click", function (e) {

            var el = $(e.target),
                isPoint = el.hasClass("linker_point"),
                isOutput = (el.data("type") === "output");

            // if there is a selected output
            // check if the new on is input point
            if (selectedOutput) {
                // connect the output and the input
                if (isPoint && !isOutput) {
                    var output = selectedOutput.data("obj"),
                        input = el.data("obj");

                    // do not connect if the input and the output in the same node
                    if (output.node !== input.node) {
                        output.connect(input);
                    }
                }

                // clear the selected output and remove the draggable path
                selectedOutput.removeClass("selected");
                selectedOutput = null;
                $(dragPath).remove();
                dragPath = null;
                lk.removeClass("drag_path");

                return;
            }

            // if no output selected yet
            // select this and add draggable path
            if (isPoint && isOutput) {
                selectedOutput = $(e.target).addClass("selected");
                dragPathPos = selectedOutput.offset();
                dragPath = drawPath(dragPathPos, dragPathPos);
                lk.addClass("drag_path");
            }
        });


        /*
         *  dragging
         */
        var dragNode, dragWidth = 0;

        lk.on("mousedown touchstart", ".linker_node > h3 .linker_node_title", function (e) {
            if (e.type === "touchstart") {
                container.css("overflow", "hidden");
            }

            if (e.target !== this) {
                return;
            }

            dragNode = $(e.target).parent().parent();
            dragWidth = dragNode.width() / 2;

        }).on("mouseup touchend", function (e) {
            if (e.type === "touchend") {
                container.css("overflow", "auto");
            }

            // trigger onDragFinish
            if (dragNode) {
                var node = dragNode.data("obj");
                if (node.onDragFinish) {
                    node.onDragFinish(parseInt(dragNode.css("left")), parseInt(dragNode.css("top")));
                }
            }

            dragNode = null;
        }).on("mousemove touchmove", function (e) {
            // drag
            if (dragNode) {
                dragNode.offset({top: e.pageY - 10, left: e.pageX - dragWidth}).trigger("drag");

                // update paths
                var node = dragNode.data("obj"),
                    nodePaths = $.extend($.extend({}, node.pathsOut), node.pathsIn);

                $.each(nodePaths, function (_, arr) {
                    $.each(arr, function (_, p) {
                        var p1 = p[1].el.offset(),
                            p2 = p[2].el.offset();
                        $(p[0]).attr("d", curve(p1.left, p1.top, p2.left, p2.top));
                    });
                });

                if (node.onDrag) {
                    node.onDrag(parseInt(dragNode.css("left")), parseInt(dragNode.css("top")));
                }
            }

            // path
            if (dragPath) {
                $(dragPath).attr("d", curve(dragPathPos.left, dragPathPos.top, e.pageX - 5, e.pageY - 5));
            }
        });

        return this;
    };
}(jQuery));